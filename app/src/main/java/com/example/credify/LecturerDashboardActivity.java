package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;

import androidx.core.content.ContextCompat;
import com.example.credify.data.model.AssignmentDetail;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ActivityLecturerDashboardBinding;
import com.example.credify.utils.WorkloadCalculator;
import com.example.credify.viewmodel.WorkloadViewModel;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.credify.ui.adapter.AssignmentDetailAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LecturerDashboardActivity extends AppCompatActivity {

    private ActivityLecturerDashboardBinding binding;
    private Lecturer currentLecturer;
    private WorkloadViewModel viewModel;
    private List<AssignmentDetail> allDetails = new ArrayList<>();
    private List<SpinnerOption> spinnerOptions = new ArrayList<>();
    private AssignmentDetailAdapter assignmentAdapter;

    static class SpinnerOption {
        String text;
        String type; // "ALL", "SESSION", "SEMESTER"
        String value;

        SpinnerOption(String text, String type, String value) {
            this.text = text;
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLecturerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WorkloadViewModel.class);

        currentLecturer = getIntent().getParcelableExtra("lecturer");
        if (currentLecturer != null) {
            binding.tvWelcome.setText("Welcome, " + currentLecturer.getLecturerName());
        }

        setupSemesterFilter();
        setupNavigation();
        setupLogout();
        setupCards();
        setupRecyclerView();
        observeViewModel();

        viewModel.fetchSemesterSessions();
        viewModel.fetchAllDetails();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Perform logout when exiting from dashboard
            new com.example.credify.data.repository.UserRepository().logout();
            super.onBackPressed();
        }
    }

    private void observeViewModel() {
        viewModel.getSemesterSessions().observe(this, sessions -> {
            spinnerOptions.clear();
            spinnerOptions.add(new SpinnerOption("All Semesters (History)", "ALL", null));

            java.util.Set<String> academicSessions = new java.util.LinkedHashSet<>();
            for (SemesterSession s : sessions) {
                academicSessions.add(s.getSession());
            }
            for (String session : academicSessions) {
                spinnerOptions.add(new SpinnerOption("Session " + session, "SESSION", session));
            }

            for (SemesterSession s : sessions) {
                spinnerOptions.add(new SpinnerOption(s.getSemester() + " " + s.getYear(), "SEMESTER", s.getSemSessionID()));
            }

            ArrayAdapter<SpinnerOption> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerSemester.setAdapter(adapter);

            if (!sessions.isEmpty()) {
                // Find latest session (last one with type "SESSION")
                int latestSessionIndex = 0; // Default to ALL if not found
                for (int i = 0; i < spinnerOptions.size(); i++) {
                    if ("SESSION".equals(spinnerOptions.get(i).type)) {
                        latestSessionIndex = i;
                    }
                }
                binding.spinnerSemester.setSelection(latestSessionIndex);
            }
        });

        viewModel.getAssignmentDetails().observe(this, details -> {
            this.allDetails = details;
            calculateWorkload();
        });
    }

    private void setupSemesterFilter() {
        binding.spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateWorkload();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        assignmentAdapter = new AssignmentDetailAdapter(new ArrayList<>(), detail -> {
            Intent intent = new Intent(this, AssignmentDetailsActivity.class);
            intent.putExtra("assignment_data", detail.getAssignment());
            intent.putExtra("is_read_only", true);
            intent.putExtra("current_user_role", "Lecturer");
            startActivity(intent);
        }, true);
        binding.rvAssignedCourses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAssignedCourses.setAdapter(assignmentAdapter);
    }

    private void calculateWorkload() {
        if (currentLecturer == null || allDetails == null) return;

        SpinnerOption selected = (SpinnerOption) binding.spinnerSemester.getSelectedItem();
        if (selected == null) return;

        double totalCredits = 0.0;
        double totalBtsa = 0.0;
        List<AssignmentDetail> filteredList = new ArrayList<>();

        for (AssignmentDetail detail : allDetails) {
            if (detail.getLecturer() == null || detail.getSemesterSession() == null) continue;
            
            // 1. Check if it belongs to current lecturer
            if (!Objects.equals(currentLecturer.getLecturerID(), detail.getLecturer().getLecturerID())) {
                continue;
            }

            // 2. Apply Spinner Filter
            if ("SESSION".equals(selected.type)) {
                if (!selected.value.equals(detail.getSemesterSession().getSession())) {
                    continue;
                }
            } else if ("SEMESTER".equals(selected.type)) {
                if (!selected.value.equals(detail.getSemesterSession().getSemSessionID())) {
                    continue;
                }
            }

            filteredList.add(detail);

            // 3. Calculate using shared engine
            if (detail.getCourse() != null && detail.getAssignment() != null) {
                // Skip Part-time assignments from calculation
                if ("Part-time".equalsIgnoreCase(detail.getAssignment().getType())) continue;

                double load = detail.getAssignment().getLoadPercentage() != null ? detail.getAssignment().getLoadPercentage() : 100.0;
                
                totalCredits += WorkloadCalculator.calculateCourseCredits(
                        detail.getCourse(),
                        load
                );
                
                totalBtsa += WorkloadCalculator.calculateCourseBTSA(
                        detail.getCourse(),
                        detail.getSection(),
                        load
                );
            }
        }

        assignmentAdapter.updateDetails(filteredList);
        binding.tvNoCourses.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);

        binding.tvTotalCredits.setText(String.format(Locale.getDefault(), "%.1f", totalCredits));
        binding.tvTotalBTSA.setText(String.format(Locale.getDefault(), "%.1f", totalBtsa));

        // Evaluation Layer (Color Coding)
        double targetCredits = WorkloadCalculator.getTargetCredit(currentLecturer);
        if (targetCredits > 0) {
            double percentage = (totalCredits / targetCredits) * 100;
            if (percentage < 90) {
                binding.tvTotalCredits.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            } else if (percentage > 110) {
                binding.tvTotalCredits.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            } else {
                binding.tvTotalCredits.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            }
        } else {
            binding.tvTotalCredits.setTextColor(ContextCompat.getColor(this, R.color.utm_maroon));
        }

        double targetBtsa = WorkloadCalculator.getTargetBTSA(currentLecturer);
        if (targetBtsa > 0) {
            if (totalBtsa < targetBtsa) {
                binding.tvTotalBTSA.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            } else {
                binding.tvTotalBTSA.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            }
        } else {
            binding.tvTotalBTSA.setTextColor(ContextCompat.getColor(this, R.color.utm_maroon));
        }
    }

    private void setupNavigation() {
        binding.btnMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));

        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already here
            } else if (id == R.id.nav_my_courses) {
                Intent intent = new Intent(this, ManageAssignmentsActivity.class);
                intent.putExtra("lecturer_id", currentLecturer.getLecturerID());
                intent.putExtra("is_read_only", true);
                startActivity(intent);
            } else if (id == R.id.nav_credit_load) {
                Intent intent = new Intent(this, LecturerCreditDetailsActivity.class);
                intent.putExtra("lecturer_data", currentLecturer);
                intent.putExtra("is_read_only", true);
                startActivity(intent);
            } else if (id == R.id.nav_btsa_load) {
                Intent intent = new Intent(this, BtsaCalculationActivity.class);
                intent.putExtra("LECTURER_ID", currentLecturer.getLecturerID());
                intent.putExtra("LECTURER_NAME", currentLecturer.getLecturerName());
                intent.putExtra("is_read_only", true);
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, LecturerDetailsActivity.class);
                intent.putExtra("lecturer_data", currentLecturer);
                intent.putExtra("is_read_only", true); 
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                binding.btnLogout.performClick();
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupLogout() {
        binding.btnLogout.setOnClickListener(v -> {
            new com.example.credify.data.repository.UserRepository().logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void setupCards() {
        binding.cardBTSA.setOnClickListener(v -> {
            if (currentLecturer == null) return;
            Intent intent = new Intent(this, BtsaCalculationActivity.class);
            intent.putExtra("LECTURER_ID", currentLecturer.getLecturerID());
            intent.putExtra("LECTURER_NAME", currentLecturer.getLecturerName());
            intent.putExtra("is_read_only", true);
            startActivity(intent);
        });

        binding.cardCredits.setOnClickListener(v -> {
            if (currentLecturer == null) return;
            Intent intent = new Intent(this, LecturerCreditDetailsActivity.class);
            intent.putExtra("lecturer_data", currentLecturer);
            intent.putExtra("is_read_only", true);
            startActivity(intent);
        });

        binding.layoutAssignedCoursesHeader.setOnClickListener(v -> {
            if (currentLecturer == null) return;
            Intent intent = new Intent(this, ManageAssignmentsActivity.class);
            intent.putExtra("lecturer_id", currentLecturer.getLecturerID());
            intent.putExtra("is_read_only", true);
            startActivity(intent);
        });

        binding.cardProfile.setOnClickListener(v -> {
            if (currentLecturer == null) return;
            Intent intent = new Intent(this, LecturerDetailsActivity.class);
            intent.putExtra("lecturer_data", currentLecturer);
            intent.putExtra("is_read_only", true);
            startActivity(intent);
        });
    }
}
