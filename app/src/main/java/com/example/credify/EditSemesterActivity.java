package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ActivityEditSemesterBinding;
import com.example.credify.viewmodel.SemesterSessionState;
import com.example.credify.viewmodel.SemesterSessionViewModel;

public class EditSemesterActivity extends AppCompatActivity {

    private ActivityEditSemesterBinding binding;
    private SemesterSessionViewModel viewModel;
    private SemesterSession semesterSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditSemesterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this)
                .get(SemesterSessionViewModel.class);

        semesterSession = getIntent()
                .getParcelableExtra("semester_session_data");

        if (semesterSession != null) {
            populateFields();
        }

        observeViewModel();

        binding.btnUpdate.setOnClickListener(v ->
                updateSemester());
    }

    private void populateFields() {

        binding.etSemSessionId.setText(
                semesterSession.getSemSessionID());

        binding.etYear.setText(
                String.valueOf(semesterSession.getYear()));

        binding.etSemester.setText(
                semesterSession.getSemester());

        binding.etSession.setText(
                semesterSession.getSession());
    }

    private void observeViewModel() {

        viewModel.getSemesterSessionState()
                .observe(this, state -> {

                    if (state instanceof SemesterSessionState.Loading) {

                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.btnUpdate.setEnabled(false);

                    } else if (state instanceof SemesterSessionState.ActionSuccess) {

                        binding.progressBar.setVisibility(View.GONE);

                        Toast.makeText(
                                this,
                                ((SemesterSessionState.ActionSuccess) state)
                                        .getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();

                    } else if (state instanceof SemesterSessionState.Error) {

                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnUpdate.setEnabled(true);

                        Toast.makeText(
                                this,
                                ((SemesterSessionState.Error) state)
                                        .getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void updateSemester() {

        String semSessionId = binding.etSemSessionId
                .getText().toString().trim();

        String yearStr = binding.etYear
                .getText().toString().trim();

        String semester = binding.etSemester
                .getText().toString().trim();

        String session = binding.etSession
                .getText().toString().trim();

        if (semSessionId.isEmpty()
                || yearStr.isEmpty()
                || semester.isEmpty()
                || session.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int year;

        try {

            year = Integer.parseInt(yearStr);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Invalid year",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        SemesterSession updatedSemester =
                new SemesterSession(
                        semSessionId,
                        year,
                        semester,
                        session
                );

        viewModel.updateSemesterSession(updatedSemester);
    }
}