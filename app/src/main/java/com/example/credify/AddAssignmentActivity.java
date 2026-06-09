package com.example.credify;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.credify.data.model.Assignment;
import com.example.credify.data.model.Course;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.Section;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ActivityAddAssignmentBinding;
import com.example.credify.viewmodel.AssignmentState;
import com.example.credify.viewmodel.AssignmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddAssignmentActivity extends AppCompatActivity {

    private ActivityAddAssignmentBinding binding;
    private AssignmentViewModel viewModel;
    
    private List<Lecturer> lecturerList = new ArrayList<>();
    private List<Course> courseList = new ArrayList<>();
    private List<Section> allSectionList = new ArrayList<>();
    private List<Section> filteredSectionList = new ArrayList<>();
    private List<SemesterSession> sessionList = new ArrayList<>();
    private String filteredProgrammeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Branding: Set Status Bar and Navigation Bar colors
        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.utm_maroon));
        getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));

        binding = ActivityAddAssignmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filteredProgrammeId = getIntent().getStringExtra("programme_id");

        setupToolbar();
        setupViewModel();
        observeViewModel();
        setupActions();
        handleBackPressed();
        
        if (filteredProgrammeId != null && !filteredProgrammeId.isEmpty()) {
            viewModel.fetchDataForSpinnersByProgramme(filteredProgrammeId);
        } else {
            viewModel.fetchDataForSpinners();
        }
        viewModel.fetchSemesterSessions();

        setupTypeSpinner();
    }

    private void setupTypeSpinner() {
        String[] types = {"Full-time", "Part-time"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerType.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            if (filteredProgrammeId != null && !filteredProgrammeId.isEmpty()) {
                viewModel.fetchDataForSpinnersByProgramme(filteredProgrammeId);
            } else {
                viewModel.fetchDataForSpinners();
            }
            viewModel.fetchSemesterSessions();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AssignmentViewModel.class);
    }

    private void observeViewModel() {
        viewModel.getSemesterSessions().observe(this, sessions -> {
            this.sessionList = sessions;
            List<String> names = new ArrayList<>();
            names.add("All Semesters/Sessions");
            for (SemesterSession s : sessions) names.add(s.getSemester() + " " + s.getSession());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerSemSessionFilter.setAdapter(adapter);
            binding.spinnerSemSessionFilter.setSelection(0);
        });

        viewModel.getSectionAssignments().observe(this, assignments -> {
            int coursePos = binding.spinnerCourse.getSelectedItemPosition();
            if (coursePos < 0 || courseList.isEmpty()) return;
            
            Course selectedCourse = courseList.get(coursePos);
            String method = selectedCourse.getMethod() != null ? selectedCourse.getMethod().toUpperCase() : "";
            
            if ("M".equals(method) || "R".equals(method) || "P".equals(method)) return;

            double totalLoad = 0;
            for (com.example.credify.data.model.Assignment a : assignments) {
                totalLoad += a.getLoadPercentage();
            }
            
            if (totalLoad >= 100.0) {
                Toast.makeText(this, "Warning: This course section already has full teaching allocation (100%).", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getLecturers().observe(this, lecturers -> {
            this.lecturerList = lecturers;
            List<String> names = new ArrayList<>();
            for (Lecturer l : lecturers) names.add(l.getLecturerName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerLecturer.setAdapter(adapter);

            String prefillId = getIntent().getStringExtra("prefill_lecturer_id");
            if (prefillId != null) {
                for (int j = 0; j < lecturers.size(); j++) {
                    if (prefillId.equals(lecturers.get(j).getLecturerID())) {
                        binding.spinnerLecturer.setSelection(j);
                        break;
                    }
                }
            }
        });

        viewModel.getCourses().observe(this, courses -> {
            this.courseList = courses;
            List<String> codes = new ArrayList<>();
            for (Course c : courses) codes.add(c.getCourseCode() + " - " + c.getCourseName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, codes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerCourse.setAdapter(adapter);
        });

        viewModel.getSections().observe(this, sections -> {
            this.allSectionList = sections;
            applySectionFilter();
        });

        viewModel.getAssignmentState().observe(this, state -> {
            if (state instanceof AssignmentState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSave.setEnabled(false);
            } else if (state instanceof AssignmentState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((AssignmentState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            } else if (state instanceof AssignmentState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(this, "Error: " + ((AssignmentState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupActions() {
        binding.btnSave.setOnClickListener(v -> saveAssignment());

        binding.spinnerSemSessionFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applySectionFilter();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        android.widget.AdapterView.OnItemSelectedListener loadChecker = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                checkSectionLoad();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        };
        binding.spinnerCourse.setOnItemSelectedListener(loadChecker);
        binding.spinnerSection.setOnItemSelectedListener(loadChecker);
    }

    private void applySectionFilter() {
        int pos = binding.spinnerSemSessionFilter.getSelectedItemPosition();
        if (pos == 0) { // All
            filteredSectionList = new ArrayList<>(allSectionList);
        } else if (pos > 0 && pos <= sessionList.size()) {
            String selectedId = sessionList.get(pos - 1).getSemSessionID();
            filteredSectionList = new ArrayList<>();
            for (Section s : allSectionList) {
                if (selectedId.equals(s.getSemSessionID())) {
                    filteredSectionList.add(s);
                }
            }
        }

        List<String> ids = new ArrayList<>();
        for (Section s : filteredSectionList) ids.add(s.getSectionID() + " (" + s.getSectionNumber() + ")");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ids);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSection.setAdapter(adapter);
    }

    private void checkSectionLoad() {
        int coursePos = binding.spinnerCourse.getSelectedItemPosition();
        int secPos = binding.spinnerSection.getSelectedItemPosition();
        if (coursePos >= 0 && secPos >= 0 && !courseList.isEmpty() && !filteredSectionList.isEmpty()) {
            viewModel.fetchAssignmentsBySection(courseList.get(coursePos).getCourseCode(), filteredSectionList.get(secPos).getSectionID());
        }
    }

    private void handleBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void saveAssignment() {
        String id = binding.etAssignmentId.getText().toString().trim();
        int lecPos = binding.spinnerLecturer.getSelectedItemPosition();
        int coursePos = binding.spinnerCourse.getSelectedItemPosition();
        int secPos = binding.spinnerSection.getSelectedItemPosition();
        String loadStr = binding.etLoadPercentage.getText().toString().trim();

        if (id.isEmpty() || lecPos < 0 || coursePos < 0 || secPos < 0 || loadStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double loadPercentage;
        try {
            loadPercentage = Double.parseDouble(loadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid load percentage", Toast.LENGTH_SHORT).show();
            return;
        }

        String lecturerId = lecturerList.get(lecPos).getLecturerID();
        String courseCode = courseList.get(coursePos).getCourseCode();
        String sectionId = filteredSectionList.get(secPos).getSectionID();
        String type = binding.spinnerType.getSelectedItem().toString();

        Assignment assignment = new Assignment(id, lecturerId, courseCode, sectionId, loadPercentage, type);
        viewModel.addAssignment(assignment);
    }
}
