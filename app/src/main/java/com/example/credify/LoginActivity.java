package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.credify.data.model.Lecturer;
import com.example.credify.databinding.LoginBinding;
import com.example.credify.viewmodel.LoginState;
import com.example.credify.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private LoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Branding: Set Status Bar and Navigation Bar colors (White theme)
        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));
        getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));

        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        observeViewModel();

        // Removed viewModel.checkSession() to prevent auto-login as requested.

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(email, password);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.resetPassword(email);
            }
        });

        // The user said "app auto logs in (not supposed to) on launch".
        // If they want to REQUIRE login every time, we should NOT call checkSession() here.
        // However, standard apps usually check session. 
        // Given the prompt "app auto logs in (not supposed to)", I will NOT call checkSession() initially,
        // so the user always sees the login screen first.
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, state -> {
            if (state instanceof LoginState.Loading) {
                binding.btnLogin.setEnabled(false);
                binding.btnLogin.setText("Logging in...");
            } else if (state instanceof LoginState.SuccessAdmin) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Log In");
                startActivity(new Intent(this, AdminDashboardActivity.class));
                finish();
            } else if (state instanceof LoginState.SuccessCoordinator) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Log In");
                Lecturer lecturer = ((LoginState.SuccessCoordinator) state).getLecturer();
                Intent intent = new Intent(this, CoordinatorDashboardActivity.class);
                intent.putExtra("lecturer", lecturer);
                startActivity(intent);
                finish();
            } else if (state instanceof LoginState.SuccessLecturer) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Log In");
                Lecturer lecturer = ((LoginState.SuccessLecturer) state).getLecturer();
                Intent intent = new Intent(this, LecturerDashboardActivity.class);
                intent.putExtra("lecturer", lecturer);
                startActivity(intent);
                finish();
            } else if (state instanceof LoginState.OtpSent) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Log In");
                String email = binding.etEmail.getText().toString().trim();
                Intent intent = new Intent(this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            } else if (state instanceof LoginState.PasswordUpdated) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Log In");
                Toast.makeText(this, "Password updated. Please log in with your new password.", Toast.LENGTH_LONG).show();
            } else if (state instanceof LoginState.Error) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Log In");
                Toast.makeText(this, ((LoginState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
