package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.credify.databinding.ActivityAddProgrammeBinding;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.ProgrammeState;
import com.example.credify.viewmodel.ProgrammeViewModel;

public class AddProgrammeActivity extends AppCompatActivity {
    private ActivityAddProgrammeBinding binding;
    private ProgrammeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProgrammeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Programme");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(ProgrammeViewModel.class);
        observeViewModel();

        binding.btnSave.setOnClickListener(v -> saveProgramme());
    }

    private void observeViewModel() {
        viewModel.getProgrammeState().observe(this, state -> {
            if (state instanceof ProgrammeState.Loading) {
                binding.btnSave.setEnabled(false);
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof ProgrammeState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                DialogUtils.showSuccessDialog(this, "PROGRAMME ADDED", ((ProgrammeState.ActionSuccess) state).getMessage(), this::finish);
            } else if (state instanceof ProgrammeState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(this, ((ProgrammeState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProgramme() {
        String id = binding.etProgrammeId.getText().toString().trim();
        String name = binding.etProgrammeName.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.addProgramme(id, name);
    }
}
