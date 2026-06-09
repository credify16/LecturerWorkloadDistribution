package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.example.credify.data.model.Assignment;
import com.example.credify.data.model.Section;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ActivityManageAssignmentsBinding;
import com.example.credify.ui.adapter.AssignmentAdapter;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.AssignmentState;
import com.example.credify.viewmodel.AssignmentViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageAssignmentsActivity extends AppCompatActivity {

    private ActivityManageAssignmentsBinding binding;
    private AssignmentViewModel viewModel;
    private AssignmentAdapter adapter;
    private String filteredLecturerId;
    private String filteredProgrammeId;
    private boolean isReadOnly = false;
    private List<Assignment> allAssignmentsList = new ArrayList<>();
    private List<Section> allSectionsList = new ArrayList<>();
    private List<SpinnerOption> spinnerOptions = new ArrayList<>();

    static class SpinnerOption {
        String text;
        String type; // "ALL", "SESSION", "SEMESTER"
        String value;

        SpinnerOption(String text, String type, String value) {
            this.text = text;
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageAssignmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filteredLecturerId = getIntent().getStringExtra("lecturer_id");
        filteredProgrammeId = getIntent().getStringExtra("programme_id");
        isReadOnly = getIntent().getBooleanExtra("is_read_only", false);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (filteredLecturerId != null) {
            binding.toolbar.setTitle("My Assignments");
        }

        viewModel = new ViewModelProvider(this).get(AssignmentViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupSemesterFilter();
        observeViewModel();

        binding.btnAddAssignment.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAssignmentActivity.class);
            intent.putExtra("programme_id", filteredProgrammeId);
            startActivity(intent);
        });
        
        // If filtered or read-only, hide the Add button
        if (filteredLecturerId != null || isReadOnly) {
            binding.btnAddAssignment.setVisibility(View.GONE);
        }

        loadData();
        viewModel.fetchSemesterSessions();
        viewModel.fetchDataForSpinners();
    }

    private void setupSemesterFilter() {
        binding.spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyLocalFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applyLocalFilters() {
        SpinnerOption selected = (SpinnerOption) binding.spinnerSemester.getSelectedItem();
        if (selected == null || allAssignmentsList == null) return;

        Map<String, Section> sectionMap = new HashMap<>();
        for (Section s : allSectionsList) {
            sectionMap.put(s.getSectionID(), s);
        }

        Map<String, String> sessionMap = new HashMap<>();
        List<SemesterSession> sessions = viewModel.getSemesterSessions().getValue();
        if (sessions != null) {
            for (SemesterSession ss : sessions) {
                sessionMap.put(ss.getSemSessionID(), ss.getSession());
            }
        }

        List<Assignment> filtered = new ArrayList<>();
        for (Assignment a : allAssignmentsList) {
            Section s = sectionMap.get(a.getSectionID());
            if (s == null) {
                if ("ALL".equals(selected.type)) filtered.add(a);
                continue;
            }

            if ("SESSION".equals(selected.type)) {
                String session = sessionMap.get(s.getSemSessionID());
                if (selected.value.equals(session)) {
                    filtered.add(a);
                }
            } else if ("SEMESTER".equals(selected.type)) {
                if (selected.value.equals(s.getSemSessionID())) {
                    filtered.add(a);
                }
            } else {
                filtered.add(a);
            }
        }
        adapter.updateAssignments(filtered);
    }

    private void loadData() {
        if (filteredLecturerId != null) {
            viewModel.fetchAssignmentsByLecturer(filteredLecturerId);
        } else if (filteredProgrammeId != null) {
            viewModel.fetchAssignmentsByProgramme(filteredProgrammeId);
        } else {
            viewModel.fetchAssignments();
        }
    }

    private void setupRecyclerView() {
        adapter = new AssignmentAdapter(
                new ArrayList<>(),
                // Edit Click -> Now View Details if Read Only
                assignment -> {
                    Intent intent = new Intent(this, AssignmentDetailsActivity.class);
                    intent.putExtra("assignment_data", assignment);
                    intent.putExtra("is_read_only", isReadOnly);
                    intent.putExtra("current_user_role", getIntent().getStringExtra("current_user_role"));
                    intent.putExtra("programme_id", filteredProgrammeId);
                    startActivity(intent);
                },
                // Delete Click
                assignment -> {
                    if (isReadOnly) {
                        Toast.makeText(this, "Lecturers cannot delete assignments", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (assignment.getAssignmentID() != null) {
                        showDeleteConfirmation(assignment.getAssignmentID());
                    }
                }
        );
        binding.rvAssignments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAssignments.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void showDeleteConfirmation(String assignmentId) {
        DialogUtils.showDeleteConfirmationDialog(this, 
            "Are you sure you want to delete this assignment?",
            () -> viewModel.deleteAssignment(assignmentId));
    }

    private void observeViewModel() {

        viewModel.getSemesterSessions().observe(this, sessions -> {
            if (sessions == null) return;
            spinnerOptions.clear();
            spinnerOptions.add(new SpinnerOption("All Semesters (History)", "ALL", null));

            java.util.Set<String> academicSessions = new java.util.LinkedHashSet<>();
            for (SemesterSession s : sessions) {
                academicSessions.add(s.getSession());
            }
            for (String session : academicSessions) {
                spinnerOptions.add(new SpinnerOption("Session " + session, "SESSION", session));
            }
            
            for (SemesterSession s : sessions) {
                spinnerOptions.add(new SpinnerOption(s.getSemester() + " " + s.getYear(), "SEMESTER", s.getSemSessionID()));
            }

            ArrayAdapter<SpinnerOption> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerSemester.setAdapter(adapter);

            if (!sessions.isEmpty()) {
                binding.spinnerSemester.setSelection(0);
            }
        });

        viewModel.getSections().observe(this, sections -> {
            this.allSectionsList = sections;
            applyLocalFilters();
        });

        viewModel.getAssignmentState().observe(this, state -> {

            if (state instanceof AssignmentState.Loading) {

                binding.progressBar
                        .setVisibility(View.VISIBLE);

                binding.layoutEmpty
                        .setVisibility(View.GONE);

            } else if (state instanceof AssignmentState.Success) {

                binding.progressBar
                        .setVisibility(View.GONE);

                binding.layoutEmpty
                        .setVisibility(View.GONE);

                this.allAssignmentsList = ((AssignmentState.Success) state).getAssignments();
                applyLocalFilters();

            } else if (state instanceof AssignmentState.Empty) {

                binding.progressBar
                        .setVisibility(View.GONE);

                binding.layoutEmpty
                        .setVisibility(View.VISIBLE);

                adapter.updateAssignments(
                        new ArrayList<>()
                );

            } else if (state instanceof AssignmentState.ActionSuccess) {

                binding.progressBar
                        .setVisibility(View.GONE);

                Toast.makeText(
                        this,
                        ((AssignmentState.ActionSuccess) state)
                                .getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

                loadData();

            } else if (state instanceof AssignmentState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(
                        this,
                        ((AssignmentState.Error) state).getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
