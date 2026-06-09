package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.credify.data.model.Programme;
import com.example.credify.databinding.ActivityEditProgrammeBinding;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.ProgrammeState;
import com.example.credify.viewmodel.ProgrammeViewModel;

public class EditProgrammeActivity extends AppCompatActivity {
    private ActivityEditProgrammeBinding binding;
    private ProgrammeViewModel viewModel;
    private Programme currentProgramme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProgrammeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentProgramme = getIntent().getParcelableExtra("programme_data");
        if (currentProgramme == null) {
            Toast.makeText(this, "Error: No programme data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Programme");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(ProgrammeViewModel.class);
        observeViewModel();

        binding.etProgrammeId.setText(currentProgramme.getProgrammeID());
        binding.etProgrammeName.setText(currentProgramme.getProgrammeName());

        binding.btnUpdate.setOnClickListener(v -> updateProgramme());
    }

    private void observeViewModel() {
        viewModel.getProgrammeState().observe(this, state -> {
            if (state instanceof ProgrammeState.Loading) {
                binding.btnUpdate.setEnabled(false);
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof ProgrammeState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                DialogUtils.showSuccessDialog(this, "PROGRAMME UPDATED", ((ProgrammeState.ActionSuccess) state).getMessage(), this::finish);
            } else if (state instanceof ProgrammeState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnUpdate.setEnabled(true);
                Toast.makeText(this, ((ProgrammeState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProgramme() {
        String name = binding.etProgrammeName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.updateProgramme(currentProgramme.getProgrammeID(), name);
    }
}
