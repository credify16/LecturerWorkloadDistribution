package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.credify.data.model.Campus;
import com.example.credify.data.model.Programme;
import com.example.credify.data.model.Section;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ActivityEditSectionBinding;
import com.example.credify.viewmodel.SectionState;
import com.example.credify.viewmodel.SectionViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditSectionActivity extends AppCompatActivity {

    private ActivityEditSectionBinding binding;
    private SectionViewModel viewModel;
    
    private Section currentSection;
    private List<Campus> campusList = new ArrayList<>();
    private List<Programme> programmeList = new ArrayList<>();
    private List<SemesterSession> sessionList = new ArrayList<>();
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditSectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserRole = getIntent().getStringExtra("current_user_role");

        viewModel = new ViewModelProvider(this).get(SectionViewModel.class);

        // 1. Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Section");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 2. Get Data from Intent
        currentSection = getIntent().getParcelableExtra("section_data");

        observeViewModel();

        if (currentSection != null) {
            populateFields();
        } else {
            Toast.makeText(this, "Error: No data found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 3. Update Button Logic
        binding.btnUpdate.setOnClickListener(v -> updateSection());
        
        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            binding.tvProgrammeLabel.setVisibility(View.VISIBLE);
            binding.spinnerProgramme.setVisibility(View.VISIBLE);
            viewModel.fetchDataForSpinners();
        } else if ("Coordinator".equalsIgnoreCase(currentUserRole)) {
            binding.tvProgrammeLabel.setVisibility(View.GONE);
            binding.spinnerProgramme.setVisibility(View.GONE);
            viewModel.fetchDataForSpinners();
        } else {
            viewModel.fetchDataForSpinners();
        }
    }

    private void populateFields() {
        binding.etSectionId.setText(currentSection.getSectionID());
        binding.etSectionId.setEnabled(false);
        binding.etSectionNumber.setText(currentSection.getSectionNumber());
        binding.etStudentAmount.setText(currentSection.getStudentAmount());
    }

    private void observeViewModel() {
        viewModel.getCampuses().observe(this, campuses -> {
            this.campusList = campuses;
            List<String> names = new ArrayList<>();
            for (Campus c : campuses) names.add(c.getCampusName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerCampus.setAdapter(adapter);

            if (currentSection != null) {
                for (int i = 0; i < campuses.size(); i++) {
                    if (campuses.get(i).getCampusID().equals(currentSection.getCampusID())) {
                        binding.spinnerCampus.setSelection(i);
                        break;
                    }
                }
            }
        });

        viewModel.getProgrammes().observe(this, programmes -> {
            this.programmeList = programmes;
            List<String> names = new ArrayList<>();
            for (Programme p : programmes) names.add(p.getProgrammeName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerProgramme.setAdapter(adapter);

            if (currentSection != null) {
                for (int i = 0; i < programmes.size(); i++) {
                    if (programmes.get(i).getProgrammeID().equals(currentSection.getProgrammeID())) {
                        binding.spinnerProgramme.setSelection(i);
                        break;
                    }
                }
            }
        });

        viewModel.getSessions().observe(this, sessions -> {
            this.sessionList = sessions;
            List<String> names = new ArrayList<>();
            for (SemesterSession s : sessions) names.add(s.getSemester() + " " + s.getSession());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerSemSession.setAdapter(adapter);

            if (currentSection != null) {
                for (int i = 0; i < sessions.size(); i++) {
                    if (sessions.get(i).getSemSessionID().equals(currentSection.getSemSessionID())) {
                        binding.spinnerSemSession.setSelection(i);
                        break;
                    }
                }
            }
        });

        viewModel.getSectionState().observe(this, state -> {
            if (state instanceof SectionState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnUpdate.setEnabled(false);
            } else if (state instanceof SectionState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((SectionState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            } else if (state instanceof SectionState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnUpdate.setEnabled(true);
                Toast.makeText(this, "Error: " + ((SectionState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSection() {
        String number = binding.etSectionNumber.getText().toString().trim();
        String studentAmtStr = binding.etStudentAmount.getText().toString().trim();
        
        int campusPos = binding.spinnerCampus.getSelectedItemPosition();
        int progPos = binding.spinnerProgramme.getSelectedItemPosition();
        int sessPos = binding.spinnerSemSession.getSelectedItemPosition();

        if (number.isEmpty() || studentAmtStr.isEmpty() || campusPos < 0 || sessPos < 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String progId;
        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            if (progPos >= 0) {
                progId = programmeList.get(progPos).getProgrammeID();
            } else {
                progId = currentSection.getProgrammeID();
            }
        } else {
            progId = currentSection.getProgrammeID();
        }

        Section section = new Section(
            currentSection.getSectionID(),
            number,
            campusList.get(campusPos).getCampusID(),
            studentAmtStr,
            progId,
            sessionList.get(sessPos).getSemSessionID()
        );

        viewModel.updateSection(section);
    }
}
