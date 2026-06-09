package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.credify.databinding.ActivityManageSemesterSessionsBinding;
import com.example.credify.ui.adapter.SemesterSessionAdapter;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.SemesterSessionState;
import com.example.credify.viewmodel.SemesterSessionViewModel;

import java.util.ArrayList;

public class ManageSemesterSessionsActivity extends AppCompatActivity {

    private ActivityManageSemesterSessionsBinding binding;
    private SemesterSessionViewModel viewModel;
    private SemesterSessionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageSemesterSessionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Semester Sessions");
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this)
                .get(SemesterSessionViewModel.class);

        setupRecyclerView();
        setupSearch();
        observeViewModel();

        binding.btnAddSemester.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    AddSemesterSessionActivity.class
            ));
        });

        viewModel.fetchSemesterSessions();
    }

    private void setupRecyclerView() {

        adapter = new SemesterSessionAdapter(
                new ArrayList<>(),

                // Edit Click
                semester -> {

                    Intent intent = new Intent(
                            this,
                            EditSemesterActivity.class
                    );

                    intent.putExtra(
                            "semester_session_data",
                            semester
                    );

                    startActivity(intent);
                },

                // Delete Click
                semester -> {

                    if (semester.getSemSessionID() != null) {

                        showDeleteConfirmation(
                                semester.getSemSessionID(),
                                semester.getSemester() + " - " + semester.getSession()
                        );
                    }
                }
        );

        binding.rvSemesterSessions.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.rvSemesterSessions.setAdapter(adapter);
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

    private void showDeleteConfirmation(String semesterId, String semesterName) {
        DialogUtils.showDeleteConfirmationDialog(this, 
            "Are you sure you want to delete semester session:\n\n" + semesterName + "? This action cannot be undone.",
            () -> viewModel.deleteSemesterSession(semesterId));
    }

    private void observeViewModel() {

        viewModel.getSemesterSessionState()
                .observe(this, state -> {

                    if (state instanceof SemesterSessionState.Loading) {

                        binding.progressBar
                                .setVisibility(View.VISIBLE);

                        binding.layoutEmpty
                                .setVisibility(View.GONE);

                    } else if (state instanceof SemesterSessionState.Success) {

                        binding.progressBar
                                .setVisibility(View.GONE);

                        binding.layoutEmpty
                                .setVisibility(View.GONE);

                        adapter.updateSemesterSessions(
                                ((SemesterSessionState.Success) state)
                                        .getSemesterSessions()
                        );

                    } else if (state instanceof SemesterSessionState.Empty) {

                        binding.progressBar
                                .setVisibility(View.GONE);

                        binding.layoutEmpty
                                .setVisibility(View.VISIBLE);

                        adapter.updateSemesterSessions(
                                new ArrayList<>()
                        );

                    } else if (state instanceof SemesterSessionState.ActionSuccess) {

                        binding.progressBar
                                .setVisibility(View.GONE);

                        Toast.makeText(
                                this,
                                ((SemesterSessionState.ActionSuccess) state)
                                        .getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();

                        viewModel.fetchSemesterSessions();

                    } else if (state instanceof SemesterSessionState.Error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(
                                this,
                                ((SemesterSessionState.Error) state).getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.fetchSemesterSessions();
    }
}