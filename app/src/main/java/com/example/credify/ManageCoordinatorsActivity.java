package com.example.credify;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.credify.data.model.Lecturer;
import com.example.credify.databinding.ActivityManageCoordinatorsBinding;
import com.example.credify.ui.adapter.LecturerAdapter;
import com.example.credify.viewmodel.LecturerState;
import com.example.credify.viewmodel.LecturerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageCoordinatorsActivity extends AppCompatActivity {

    private ActivityManageCoordinatorsBinding binding;
    private LecturerViewModel viewModel;
    private LecturerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageCoordinatorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();

        viewModel = new ViewModelProvider(this)
                .get(LecturerViewModel.class);

        setupRecyclerView();
        setupSearch();
        observeViewModel();
        setupActions();
    }

    private void setupToolbar() {

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {

            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);

            getSupportActionBar()
                    .setTitle("Manage Coordinators");
        }

        binding.toolbar.setNavigationOnClickListener(
                v -> onBackPressed()
        );
    }

    private void setupRecyclerView() {

        adapter = new LecturerAdapter(
                new ArrayList<>(),

                // Edit Click
                lecturer -> {

                    Intent intent = new Intent(
                            this,
                            EditLecturerActivity.class
                    );

                    intent.putExtra(
                            "lecturer_data",
                            lecturer
                    );

                    startActivity(intent);
                },

                // Delete Click
                lecturer -> {

                    if (lecturer.getLecturerID() != null) {

                        showDeleteConfirmation(
                                lecturer.getLecturerID(),
                                lecturer.getLecturerName()
                        );
                    }
                }
        );

        binding.rvCoordinators.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.rvCoordinators.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupActions() {

        binding.btnAddCoordinator.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            this,
                            AddLecturerActivity.class
                    )
            );

        });
    }

    private void showDeleteConfirmation(
            String lecturerId,
            String lecturerName
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Coordinator")
                .setMessage(
                        "Are you sure you want to delete coordinator:\n\n"
                                + lecturerName
                                + " ?"
                )
                .setPositiveButton("Delete",
                        (dialog, which) -> {

                            viewModel.deleteLecturer(
                                    lecturerId
                            );

                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {

        viewModel.getLecturerState().observe(this, state -> {

            if (state instanceof LecturerState.Loading) {

                binding.progressBar
                        .setVisibility(View.VISIBLE);

            } else if (state instanceof LecturerState.Success) {

                binding.progressBar
                        .setVisibility(View.GONE);

                List<Lecturer> all =
                        ((LecturerState.Success) state)
                                .getLecturers();

                List<Lecturer> coordinators =
                        all.stream()
                                .filter(l ->
                                        "Course Coordinator"
                                                .equalsIgnoreCase(
                                                        l.getLecturerRole()
                                                )
                                )
                                .collect(Collectors.toList());

                if (coordinators.isEmpty()) {

                    binding.layoutEmpty
                            .setVisibility(View.VISIBLE);

                    adapter.updateLecturers(
                            new ArrayList<>()
                    );

                } else {

                    binding.layoutEmpty
                            .setVisibility(View.GONE);

                    adapter.updateLecturers(
                            coordinators
                    );
                }

            } else if (state instanceof LecturerState.Empty) {

                binding.progressBar
                        .setVisibility(View.GONE);

                binding.layoutEmpty
                        .setVisibility(View.VISIBLE);

                adapter.updateLecturers(
                        new ArrayList<>()
                );

            } else if (state instanceof LecturerState.ActionSuccess) {

                binding.progressBar
                        .setVisibility(View.GONE);

                Toast.makeText(
                        this,
                        ((LecturerState.ActionSuccess) state)
                                .getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

                viewModel.fetchLecturers();

            } else if (state instanceof LecturerState.Error) {

                binding.progressBar
                        .setVisibility(View.GONE);

                String errorMessage =
                        ((LecturerState.Error) state)
                                .getMessage();

                if (errorMessage != null &&
                        (
                                errorMessage.toLowerCase().contains("foreign key") ||
                                        errorMessage.toLowerCase().contains("violates") ||
                                        errorMessage.toLowerCase().contains("constraint")
                        )
                ) {

                    Toast.makeText(
                            this,
                            "Cannot delete this coordinator because they are assigned to courses.",
                            Toast.LENGTH_LONG
                    ).show();

                } else {

                    Toast.makeText(
                            this,
                            errorMessage,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewModel.fetchLecturers();
    }
}