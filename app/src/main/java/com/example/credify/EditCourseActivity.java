package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.credify.data.model.Course;
import com.example.credify.data.model.Programme;
import com.example.credify.databinding.ActivityEditCourseBinding;
import com.example.credify.viewmodel.CourseState;
import com.example.credify.viewmodel.CourseViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditCourseActivity extends AppCompatActivity {

    private ActivityEditCourseBinding binding;
    private CourseViewModel viewModel;
    
    private Course currentCourse;
    private List<Programme> programmeList = new ArrayList<>();
    private final String[] methodOptions = {"Lecture + Lab/Practical (Biasa)", "Lecture Only (Kuliah/Khas)", "Project", "Industrial Training", "Report"};
    private final String[] methodValues = {"B", "K", "P", "M", "R"};
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCourseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserRole = getIntent().getStringExtra("current_user_role");
        viewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Course");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        currentCourse = getIntent().getParcelableExtra("course_data");

        setupSpinners();
        observeViewModel();

        if (currentCourse != null) {
            populateFields();
        } else {
            Toast.makeText(this, "Error: No course data found", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnUpdate.setOnClickListener(v -> updateCourse());

        binding.spinnerCoordinator.setVisibility(View.GONE);
        binding.tvCoordinatorLabel.setVisibility(View.GONE);
        
        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            binding.tvProgrammeLabel.setVisibility(View.VISIBLE);
            binding.spinnerProgramme.setVisibility(View.VISIBLE);
            viewModel.fetchDataForSpinners();
        } else {
            binding.tvProgrammeLabel.setVisibility(View.GONE);
            binding.spinnerProgramme.setVisibility(View.GONE);
            viewModel.fetchDataForSpinners();
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, methodOptions);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMethod.setAdapter(methodAdapter);
    }

    private void populateFields() {
        binding.etCourseCode.setText(currentCourse.getCourseCode());
        binding.etCourseCode.setEnabled(false);
        binding.etCourseName.setText(currentCourse.getCourseName());
        binding.etWeeklyHour.setText(String.valueOf(currentCourse.getWeeklyHour() != null ? currentCourse.getWeeklyHour() : 0.0));
        binding.etCreditHours.setText(String.valueOf(currentCourse.getCreditValue() != null ? currentCourse.getCreditValue() : 0.0));

        for (int i = 0; i < methodValues.length; i++) {
            if (methodValues[i].equalsIgnoreCase(currentCourse.getMethod())) {
                binding.spinnerMethod.setSelection(i);
                break;
            }
        }
    }

    private void observeViewModel() {
        viewModel.getProgrammes().observe(this, programmes -> {
            this.programmeList = programmes;
            List<String> names = new ArrayList<>();
            for (Programme p : programmes) names.add(p.getProgrammeName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerProgramme.setAdapter(adapter);

            if (currentCourse != null) {
                for (int i = 0; i < programmes.size(); i++) {
                    if (programmes.get(i).getProgrammeID().equals(currentCourse.getProgrammeID())) {
                        binding.spinnerProgramme.setSelection(i);
                        break;
                    }
                }
            }
        });

        viewModel.getCourseState().observe(this, state -> {
            if (state instanceof CourseState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnUpdate.setEnabled(false);
            } else if (state instanceof CourseState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((CourseState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            } else if (state instanceof CourseState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnUpdate.setEnabled(true);
                Toast.makeText(this, "Error: " + ((CourseState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCourse() {
        String name = binding.etCourseName.getText().toString().trim();
        int methodPos = binding.spinnerMethod.getSelectedItemPosition();
        int progPos = binding.spinnerProgramme.getSelectedItemPosition();

        if (name.isEmpty() || methodPos < 0) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String progId;
        if ("Admin".equalsIgnoreCase(currentUserRole) && progPos >= 0) {
            progId = programmeList.get(progPos).getProgrammeID();
        } else {
            progId = currentCourse.getProgrammeID();
        }

        Course course = new Course(
            currentCourse.getCourseCode(),
            name,
            methodValues[methodPos],
            parseQuietlyDouble(binding.etCreditHours.getText().toString()),
            parseQuietlyDouble(binding.etWeeklyHour.getText().toString()),
            progId
        );

        viewModel.updateCourse(course);
    }

    private Double parseQuietlyDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
    }
}
