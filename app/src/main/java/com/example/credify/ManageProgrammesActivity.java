package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.credify.databinding.ActivityManageProgrammesBinding;
import com.example.credify.ui.adapter.ProgrammeAdapter;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.ProgrammeState;
import com.example.credify.viewmodel.ProgrammeViewModel;
import java.util.ArrayList;

public class ManageProgrammesActivity extends AppCompatActivity {
    private ActivityManageProgrammesBinding binding;
    private ProgrammeViewModel viewModel;
    private ProgrammeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageProgrammesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Programmes");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(ProgrammeViewModel.class);
        setupRecyclerView();
        setupSearch();
        observeViewModel();

        binding.btnAddProgramme.setOnClickListener(v -> {
            startActivity(new Intent(this, AddProgrammeActivity.class));
        });

        viewModel.fetchProgrammes();
    }

    private void setupRecyclerView() {
        adapter = new ProgrammeAdapter(new ArrayList<>(),
                p -> {
                    Intent intent = new Intent(this, EditProgrammeActivity.class);
                    intent.putExtra("programme_data", p);
                    startActivity(intent);
                },
                p -> showDeleteConfirmation(p.getProgrammeID())
        );
        binding.rvProgrammes.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProgrammes.setAdapter(adapter);
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

    private void showDeleteConfirmation(String id) {
        DialogUtils.showDeleteConfirmationDialog(this, "Are you sure you want to delete this programme?",
                () -> viewModel.deleteProgramme(id));
    }

    private void observeViewModel() {
        viewModel.getProgrammeState().observe(this, state -> {
            if (state instanceof ProgrammeState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
            } else if (state instanceof ProgrammeState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.GONE);
                adapter.updateProgrammes(((ProgrammeState.Success) state).getProgrammes());
            } else if (state instanceof ProgrammeState.Empty) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                adapter.updateProgrammes(new ArrayList<>());
            } else if (state instanceof ProgrammeState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((ProgrammeState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                viewModel.fetchProgrammes();
            } else if (state instanceof ProgrammeState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((ProgrammeState.Error) state).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.fetchProgrammes();
    }
}
