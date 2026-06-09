package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.credify.data.model.Assignment;
import com.example.credify.data.model.Course;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.Section;
import com.example.credify.databinding.ActivityEditAssignmentBinding;
import com.example.credify.viewmodel.AssignmentState;
import com.example.credify.viewmodel.AssignmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditAssignmentActivity extends AppCompatActivity {

    private ActivityEditAssignmentBinding binding;
    private AssignmentViewModel viewModel;
    
    private Assignment currentAssignment;
    private List<Lecturer> lecturerList = new ArrayList<>();
    private List<Course> courseList = new ArrayList<>();
    private List<Section> sectionList = new ArrayList<>();
    private String currentUserRole;
    private String programmeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAssignmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserRole = getIntent().getStringExtra("current_user_role");
        programmeId = getIntent().getStringExtra("programme_id");

        viewModel = new ViewModelProvider(this).get(AssignmentViewModel.class);

        // 1. Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Assignment");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 2. Get Data from Intent
        currentAssignment = getIntent().getParcelableExtra("assignment_data");

        observeViewModel();

        if (currentAssignment != null) {
            populateFields();
        } else {
            Toast.makeText(this, "Error: No data to edit", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 3. Update Button Logic
        binding.btnUpdate.setOnClickListener(v -> updateAssignment());
        
        if (programmeId != null && !programmeId.isEmpty()) {
            viewModel.fetchDataForSpinnersByProgramme(programmeId);
        } else {
            viewModel.fetchDataForSpinners();
        }

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
            if (programmeId != null && !programmeId.isEmpty()) {
                viewModel.fetchDataForSpinnersByProgramme(programmeId);
            } else {
                viewModel.fetchDataForSpinners();
            }
        }
    }

    private void populateFields() {
        binding.etAssignmentId.setText(currentAssignment.getAssignmentID());
        binding.etLoadPercentage.setText(String.valueOf(currentAssignment.getLoadPercentage()));
        
        if (currentAssignment.getType() != null) {
            String type = currentAssignment.getType();
            if ("Part-time".equalsIgnoreCase(type)) {
                binding.spinnerType.setSelection(1);
            } else {
                binding.spinnerType.setSelection(0);
            }
        }
    }

    private void observeViewModel() {
        viewModel.getLecturers().observe(this, lecturers -> {
            this.lecturerList = lecturers;
            List<String> names = new ArrayList<>();
            for (Lecturer l : lecturers) names.add(l.getLecturerName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerLecturer.setAdapter(adapter);

            if (currentAssignment != null) {
                for (int i = 0; i < lecturers.size(); i++) {
                    if (lecturers.get(i).getLecturerID().equals(currentAssignment.getLecturerID())) {
                        binding.spinnerLecturer.setSelection(i);
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

            if (currentAssignment != null) {
                for (int i = 0; i < courses.size(); i++) {
                    if (courses.get(i).getCourseCode().equals(currentAssignment.getCourseCode())) {
                        binding.spinnerCourse.setSelection(i);
                        break;
                    }
                }
            }
        });

        viewModel.getSections().observe(this, sections -> {
            this.sectionList = sections;
            List<String> ids = new ArrayList<>();
            for (Section s : sections) ids.add(s.getSectionID() + " (" + s.getSectionNumber() + ")");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ids);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerSection.setAdapter(adapter);

            if (currentAssignment != null) {
                for (int i = 0; i < sections.size(); i++) {
                    if (sections.get(i).getSectionID().equals(currentAssignment.getSectionID())) {
                        binding.spinnerSection.setSelection(i);
                        break;
                    }
                }
            }
        });

        viewModel.getAssignmentState().observe(this, state -> {
            if (state instanceof AssignmentState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnUpdate.setEnabled(false);
            } else if (state instanceof AssignmentState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((AssignmentState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            } else if (state instanceof AssignmentState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnUpdate.setEnabled(true);
                Toast.makeText(this, "Error: " + ((AssignmentState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAssignment() {
        String loadStr = binding.etLoadPercentage.getText().toString().trim();
        int lecPos = binding.spinnerLecturer.getSelectedItemPosition();
        int coursePos = binding.spinnerCourse.getSelectedItemPosition();
        int secPos = binding.spinnerSection.getSelectedItemPosition();

        if (loadStr.isEmpty() || lecPos < 0 || coursePos < 0 || secPos < 0) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double loadPercentage = Double.parseDouble(loadStr);
            String type = binding.spinnerType.getSelectedItem().toString();
            
            Assignment assignment = new Assignment(
                currentAssignment.getAssignmentID(),
                lecturerList.get(lecPos).getLecturerID(),
                courseList.get(coursePos).getCourseCode(),
                sectionList.get(secPos).getSectionID(),
                loadPercentage,
                type
            );

            viewModel.updateAssignment(assignment);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid load percentage", Toast.LENGTH_SHORT).show();
        }
    }
}
