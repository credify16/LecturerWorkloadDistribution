package com.example.credify;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.credify.data.model.Course;
import com.example.credify.databinding.ActivityManageCoursesBinding;
import com.example.credify.ui.adapter.CourseAdapter;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.CourseState;
import com.example.credify.viewmodel.CourseViewModel;

import java.util.ArrayList;

public class ManageCoursesActivity extends AppCompatActivity {

    private ActivityManageCoursesBinding binding;
    private CourseViewModel viewModel;
    private CourseAdapter adapter;
    private String filteredProgrammeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageCoursesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        filteredProgrammeId = getIntent().getStringExtra("programme_id");

        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupSearch();
        observeViewModel();
        setupActions();

        loadData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                    }
                }
        );
    }

    private void setupViewModel() {

        viewModel = new ViewModelProvider(this)
                .get(CourseViewModel.class);
    }

    private void setupRecyclerView() {

        adapter = new CourseAdapter(
                new ArrayList<>(),

                // Edit / View
                course -> {

                    Intent intent = new Intent(
                            this,
                            CourseDetailsActivity.class
                    );

                    intent.putExtra(
                            "course_data",
                            course
                    );
                    
                    intent.putExtra("current_user_role", getIntent().getStringExtra("current_user_role"));

                    startActivity(intent);
                },

                // Delete
                course -> {

                    if (course.getCourseCode() != null) {

                        showDeleteConfirmation(
                                course.getCourseCode(),
                                course.getCourseName()
                        );
                    }
                }
        );

        binding.rvCourses.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.rvCourses.setAdapter(adapter);
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

    private void showDeleteConfirmation(String courseCode, String courseName) {
        DialogUtils.showDeleteConfirmationDialog(this, 
            "Are you sure you want to delete course:\n\n" + courseName + "? This action cannot be undone.",
            () -> viewModel.deleteCourse(courseCode));
    }

    private void setupActions() {

        binding.btnAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCourseActivity.class);
            intent.putExtra("programme_id", filteredProgrammeId);
            startActivity(intent);
        });
    }

    private void observeViewModel() {

        viewModel.getCourseState().observe(this, state -> {

            if (state instanceof CourseState.Loading) {

                binding.progressBar
                        .setVisibility(View.VISIBLE);

                binding.layoutEmpty
                        .setVisibility(View.GONE);

            } else if (state instanceof CourseState.Success) {

                binding.progressBar
                        .setVisibility(View.GONE);

                binding.layoutEmpty
                        .setVisibility(View.GONE);

                adapter.updateCourses(
                        ((CourseState.Success) state)
                                .getCourses()
                );

            } else if (state instanceof CourseState.Empty) {

                binding.progressBar
                        .setVisibility(View.GONE);

                binding.layoutEmpty
                        .setVisibility(View.VISIBLE);

                adapter.updateCourses(
                        new ArrayList<>()
                );

            } else if (state instanceof CourseState.ActionSuccess) {

                binding.progressBar
                        .setVisibility(View.GONE);

                Toast.makeText(
                        this,
                        ((CourseState.ActionSuccess) state)
                                .getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

                loadData();

            } else if (state instanceof CourseState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(
                        this,
                        ((CourseState.Error) state).getMessage(),
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

    private void loadData() {
        if (filteredProgrammeId != null) {
            viewModel.fetchCoursesByProgramme(filteredProgrammeId);
        } else {
            viewModel.fetchCourses();
        }
    }
}
