package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.credify.databinding.ActivityEditAdminBinding;
import com.example.credify.viewmodel.AdminState;
import com.example.credify.viewmodel.AdminViewModel;

public class EditAdminActivity extends AppCompatActivity {

    private ActivityEditAdminBinding binding;
    private AdminViewModel viewModel;
    private String adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // 1. Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Admin");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 2. Get Data from Intent
        adminId = getIntent().getStringExtra("admin_id");
        String adminName = getIntent().getStringExtra("admin_name");
        String adminEmail = getIntent().getStringExtra("admin_email");

        if (adminId != null) {
            binding.etAdminId.setText(adminId);
            binding.etAdminName.setText(adminName);
            binding.etEmail.setText(adminEmail);
        }

        observeViewModel();

        // 3. Update Button Logic
        binding.btnUpdate.setOnClickListener(v -> updateAdmin());
    }

    private void observeViewModel() {
        viewModel.getAdminState().observe(this, state -> {
            if (state instanceof AdminState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnUpdate.setEnabled(false);
            } else if (state instanceof AdminState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((AdminState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            } else if (state instanceof AdminState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnUpdate.setEnabled(true);
                Toast.makeText(this, "Error: " + ((AdminState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAdmin() {
        String name = binding.etAdminName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Admin name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.updateAdmin(adminId, name, email, password);
    }
}
