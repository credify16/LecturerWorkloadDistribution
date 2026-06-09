package com.example.credify;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.credify.data.model.Course;
import com.example.credify.data.model.Programme;
import com.example.credify.databinding.ActivityAddCourseBinding;
import com.example.credify.viewmodel.CourseState;
import com.example.credify.viewmodel.CourseViewModel;
import java.util.ArrayList;
import java.util.List;

public class AddCourseActivity extends AppCompatActivity {

    private ActivityAddCourseBinding binding;
    private CourseViewModel viewModel;
    private List<Programme> programmeList = new ArrayList<>();
    private String filteredProgrammeId;
    
    private final String[] methodOptions = {"Lecture + Lab/Practical (Biasa)", "Lecture Only (Kuliah/Khas)", "Project", "Industrial Training", "Report"};
    private final String[] methodValues = {"B", "K", "P", "M", "R"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCourseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filteredProgrammeId = getIntent().getStringExtra("programme_id");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        setupSpinners();
        observeViewModel();
        
        binding.btnSave.setOnClickListener(v -> saveCourse());
        
        binding.spinnerCoordinator.setVisibility(View.GONE);
        binding.tvCoordinatorLabel.setVisibility(View.GONE);

        if (filteredProgrammeId != null && !filteredProgrammeId.isEmpty()) {
            binding.tvProgrammeLabel.setVisibility(View.GONE);
            binding.spinnerProgramme.setVisibility(View.GONE);
        } else {
            viewModel.fetchDataForSpinners();
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, methodOptions);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMethod.setAdapter(methodAdapter);
    }

    private void observeViewModel() {
        viewModel.getProgrammes().observe(this, programmes -> {
            this.programmeList = programmes;
            List<String> names = new ArrayList<>();
            for (Programme p : programmes) names.add(p.getProgrammeName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerProgramme.setAdapter(adapter);
        });

        viewModel.getCourseState().observe(this, state -> {
            if (state instanceof CourseState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSave.setEnabled(false);
            } else if (state instanceof CourseState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((CourseState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            } else if (state instanceof CourseState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(this, "Error: " + ((CourseState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCourse() {
        try {
            String code = binding.etCourseCode.getText().toString().trim();
            String name = binding.etCourseName.getText().toString().trim();
            int methodPos = binding.spinnerMethod.getSelectedItemPosition();
            int progPos = binding.spinnerProgramme.getSelectedItemPosition();

            if (code.isEmpty() || name.isEmpty() || methodPos < 0) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
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

            String method = methodValues[methodPos];

            Course course = new Course(
                code, name, method,
                parseQuietlyDouble(binding.etCredits.getText().toString()),
                parseQuietlyDouble(binding.etWeeklyHour.getText().toString()),
                progId
            );

            viewModel.addCourse(course);
        } catch (Exception e) {
            Log.e("AddCourseActivity", "Save Error", e);
        }
    }

    private Double parseQuietlyDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
    }
}
