package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.data.repository.SemesterSessionRepository;
import com.example.credify.databinding.ActivityAddSemesterSessionBinding;

public class AddSemesterSessionActivity extends AppCompatActivity {

    private ActivityAddSemesterSessionBinding binding;
    private final SemesterSessionRepository repository = new SemesterSessionRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddSemesterSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        binding.btnSave.setOnClickListener(v -> saveSession());
    }

    private void saveSession() {
        String id = binding.etSemSessionID.getText().toString().trim();
        String yearStr = binding.etYear.getText().toString().trim();
        String semester = binding.etSemester.getText().toString().trim();
        String session = binding.etSession.getText().toString().trim();

        if (id.isEmpty() || yearStr.isEmpty() || semester.isEmpty() || session.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid year", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        SemesterSession semSession = new SemesterSession(id, year, semester, session);
        repository.addSemesterSession(semSession).thenAccept(success -> {
            runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                if (success) {
                    Toast.makeText(this, "Session added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, "Failed to add session", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
