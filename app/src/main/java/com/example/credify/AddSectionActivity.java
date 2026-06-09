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
import com.example.credify.databinding.ActivityAddSectionBinding;
import com.example.credify.viewmodel.SectionState;
import com.example.credify.viewmodel.SectionViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddSectionActivity extends AppCompatActivity {

    private ActivityAddSectionBinding binding;
    private SectionViewModel viewModel;
    
    private List<Campus> campusList = new ArrayList<>();
    private List<Programme> programmeList = new ArrayList<>();
    private List<SemesterSession> sessionList = new ArrayList<>();
    private String filteredProgrammeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddSectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filteredProgrammeId = getIntent().getStringExtra("programme_id");

        viewModel = new ViewModelProvider(this).get(SectionViewModel.class);

        setupToolbar();
        observeViewModel();

        binding.btnSave.setOnClickListener(v -> saveSection());
        
        if (filteredProgrammeId != null && !filteredProgrammeId.isEmpty()) {
            binding.tvProgrammeLabel.setVisibility(View.GONE);
            binding.spinnerProgramme.setVisibility(View.GONE);
            viewModel.fetchDataForSpinners(); // Still need other spinners
        } else {
            viewModel.fetchDataForSpinners();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Section");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void observeViewModel() {
        viewModel.getCampuses().observe(this, campuses -> {
            this.campusList = campuses;
            List<String> names = new ArrayList<>();
            for (Campus c : campuses) names.add(c.getCampusName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerCampus.setAdapter(adapter);
        });

        viewModel.getProgrammes().observe(this, programmes -> {
            this.programmeList = programmes;
            List<String> names = new ArrayList<>();
            for (Programme p : programmes) names.add(p.getProgrammeName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerProgramme.setAdapter(adapter);
        });

        viewModel.getSessions().observe(this, sessions -> {
            this.sessionList = sessions;
            List<String> names = new ArrayList<>();
            for (SemesterSession s : sessions) names.add(s.getSemester() + " " + s.getSession());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerSemSession.setAdapter(adapter);
        });

        viewModel.getSectionState().observe(this, state -> {
            if (state instanceof SectionState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSave.setEnabled(false);
            } else if (state instanceof SectionState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((SectionState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            } else if (state instanceof SectionState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(this, "Error: " + ((SectionState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSection() {
        String id = binding.etSectionId.getText().toString().trim();
        String number = binding.etSectionNumber.getText().toString().trim();
        String studentAmtStr = binding.etStudentAmount.getText().toString().trim();
        
        int campusPos = binding.spinnerCampus.getSelectedItemPosition();
        int progPos = binding.spinnerProgramme.getSelectedItemPosition();
        int sessPos = binding.spinnerSemSession.getSelectedItemPosition();

        if (id.isEmpty() || number.isEmpty() || studentAmtStr.isEmpty() || campusPos < 0 || sessPos < 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String progId;
        if (filteredProgrammeId != null && !filteredProgrammeId.isEmpty()) {
            progId = filteredProgrammeId;
        } else {
            if (progPos < 0) {
                Toast.makeText(this, "Please select a programme", Toast.LENGTH_SHORT).show();
                return;
            }
            progId = programmeList.get(progPos).getProgrammeID();
        }

        Section section = new Section(
            id,
            number,
            campusList.get(campusPos).getCampusID(),
            studentAmtStr, // Store as string (might be formula)
            progId,
            sessionList.get(sessPos).getSemSessionID()
        );

        viewModel.addSection(section);
    }
}
