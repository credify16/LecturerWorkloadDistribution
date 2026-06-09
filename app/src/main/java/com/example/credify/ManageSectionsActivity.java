package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.credify.data.model.Section;
import com.example.credify.databinding.ActivityManageSectionsBinding;
import com.example.credify.ui.adapter.SectionAdapter;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.SectionState;
import com.example.credify.viewmodel.SectionViewModel;

import java.util.ArrayList;

public class ManageSectionsActivity extends AppCompatActivity {

    private ActivityManageSectionsBinding binding;
    private SectionViewModel viewModel;
    private SectionAdapter adapter;
    private String filteredProgrammeId;
    private java.util.List<com.example.credify.data.model.SemesterSession> sessionList = new java.util.ArrayList<>();
    private java.util.List<Section> allSections = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageSectionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filteredProgrammeId = getIntent().getStringExtra("programme_id");

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Sections");
        }

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(SectionViewModel.class);

        setupRecyclerView();
        setupSearch();
        observeViewModel();
        setupFilters();

        binding.btnAddSection.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddSectionActivity.class);
            intent.putExtra("programme_id", filteredProgrammeId);
            startActivity(intent);
        });

        loadData();
        viewModel.fetchDataForSpinners();
    }

    private void setupFilters() {
        binding.spinnerSemSession.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void applyFilters() {
        int pos = binding.spinnerSemSession.getSelectedItemPosition();
        if (pos == 0) { // "All"
            adapter.updateSections(allSections);
        } else if (pos > 0 && pos <= sessionList.size()) {
            String selectedSessionId = sessionList.get(pos - 1).getSemSessionID();
            java.util.List<Section> filtered = new java.util.ArrayList<>();
            for (Section s : allSections) {
                if (selectedSessionId.equals(s.getSemSessionID())) {
                    filtered.add(s);
                }
            }
            adapter.updateSections(filtered);
        }
        adapter.filter(binding.etSearch.getText().toString());
    }

    private void setupRecyclerView() {
        adapter = new SectionAdapter(
                new ArrayList<>(),
                // View Details Click
                section -> {
                    Intent intent = new Intent(this, SectionDetailsActivity.class);
                    intent.putExtra("section_data", section);
                    intent.putExtra("current_user_role", getIntent().getStringExtra("current_user_role"));
                    startActivity(intent);
                },
                // Delete Click
                section -> {
                    if (section.getSectionID() != null) {
                        showDeleteConfirmation(section.getSectionID(), "Section " + section.getSectionNumber());
                    }
                }
        );
        binding.rvSections.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSections.setAdapter(adapter);
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

    private void showDeleteConfirmation(String sectionId, String sectionName) {
        DialogUtils.showDeleteConfirmationDialog(this, 
            "Are you sure you want to delete section:\n\n" + sectionName + "? This action cannot be undone.",
            () -> viewModel.deleteSection(sectionId));
    }

    private void observeViewModel() {
        viewModel.getSessions().observe(this, sessions -> {
            this.sessionList = sessions;
            java.util.List<String> names = new java.util.ArrayList<>();
            names.add("All Semesters/Sessions"); // Default to All
            for (com.example.credify.data.model.SemesterSession s : sessions) {
                names.add(s.getSemester() + " " + s.getSession());
            }
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerSemSession.setAdapter(adapter);
            binding.spinnerSemSession.setSelection(0); // Set default to All
        });

        viewModel.getSectionState().observe(this, state -> {
            if (state instanceof SectionState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
            } else if (state instanceof SectionState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.GONE);
                this.allSections = ((SectionState.Success) state).getSections();
                applyFilters();
            } else if (state instanceof SectionState.Empty) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                this.allSections = new ArrayList<>();
                adapter.updateSections(new ArrayList<>());
            } else if (state instanceof SectionState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((SectionState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                loadData();
            } else if (state instanceof SectionState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((SectionState.Error) state).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (filteredProgrammeId != null) {
            viewModel.fetchSectionsByProgramme(filteredProgrammeId);
        } else {
            viewModel.fetchSections();
        }
    }
}
