package com.example.credify;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.credify.databinding.ActivityResetPasswordBinding;
import com.example.credify.viewmodel.LoginState;
import com.example.credify.viewmodel.LoginViewModel;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private LoginViewModel viewModel;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Branding: Set Status Bar and Navigation Bar colors
        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.utm_maroon));
        getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));

        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        email = getIntent().getStringExtra("email");
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        observeViewModel();

        binding.btnReset.setOnClickListener(v -> {
            String otp = binding.etOtp.getText().toString().trim();
            String newPassword = binding.etNewPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (otp.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.verifyOtpAndUpdatePassword(email, otp, newPassword);
        });
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, state -> {
            if (state instanceof LoginState.Loading) {
                binding.btnReset.setEnabled(false);
                binding.btnReset.setText("Updating...");
            } else if (state instanceof LoginState.PasswordUpdated) {
                binding.btnReset.setEnabled(true);
                binding.btnReset.setText("Reset Password");
                Toast.makeText(this, "Password updated successfully. Please log in.", Toast.LENGTH_LONG).show();
                finish();
            } else if (state instanceof LoginState.Error) {
                binding.btnReset.setEnabled(true);
                binding.btnReset.setText("Reset Password");
                Toast.makeText(this, ((LoginState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
