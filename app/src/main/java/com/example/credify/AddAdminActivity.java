package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.credify.databinding.ActivityAddAdminBinding;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.AdminState;
import com.example.credify.viewmodel.AdminViewModel;

public class AddAdminActivity extends AppCompatActivity {

    private ActivityAddAdminBinding binding;
    private AdminViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Admin");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        observeViewModel();
        binding.btnSaveAdmin.setOnClickListener(v -> saveAdmin());
    }

    private void observeViewModel() {
        viewModel.getAdminState().observe(this, state -> {
            if (state instanceof AdminState.Loading) {
                binding.btnSaveAdmin.setEnabled(false);
            } else if (state instanceof AdminState.ActionSuccess) {
                DialogUtils.showSuccessDialog(this, "ADMIN ADDED", 
                    ((AdminState.ActionSuccess) state).getMessage(), 
                    this::finish);
            } else if (state instanceof AdminState.Error) {
                binding.btnSaveAdmin.setEnabled(true);
                Toast.makeText(this, "Error: " + ((AdminState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAdmin() {
        String id = binding.etAdminId.getText().toString().trim();
        String name = binding.etAdminName.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.addAdmin(id, name, password, email);
    }
}
