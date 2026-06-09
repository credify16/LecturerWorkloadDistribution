package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.credify.data.model.Lecturer;
import com.example.credify.databinding.ActivityCoordinatorDashboardBinding;

public class CoordinatorDashboardActivity extends AppCompatActivity {

    private ActivityCoordinatorDashboardBinding binding;
    private Lecturer currentCoordinator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCoordinatorDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentCoordinator = getIntent().getParcelableExtra("lecturer");
        if (currentCoordinator != null) {
            binding.tvWelcome.setText("Welcome, " + currentCoordinator.getLecturerName());
        }

        setupAddSpinner();
        setupNavigation();
        setupLogout();
        setupCards();
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

    private void setupAddSpinner() {
        String[] addOptions = {
                "Select Action",
                "Add Assignment",
                "Add Course",
                "Add Lecturer",
                "Add Section"

        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, addOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAdd.setAdapter(adapter);

        binding.spinnerAdd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        Intent intent1 = new Intent(CoordinatorDashboardActivity.this, AddAssignmentActivity.class);
                        intent1.putExtra("current_user_role", "Coordinator");
                        intent1.putExtra("programme_id", currentCoordinator.getProgrammeID());
                        startActivity(intent1);
                        break;
                    case 2:
                        Intent intent2 = new Intent(CoordinatorDashboardActivity.this, AddCourseActivity.class);
                        intent2.putExtra("current_user_role", "Coordinator");
                        intent2.putExtra("programme_id", currentCoordinator.getProgrammeID());
                        startActivity(intent2);
                        break;
                    case 3:
                        Intent intent3 = new Intent(CoordinatorDashboardActivity.this, AddLecturerActivity.class);
                        intent3.putExtra("current_user_role", "Coordinator");
                        intent3.putExtra("programme_id", currentCoordinator.getProgrammeID());
                        startActivity(intent3);
                        break;
                    case 4:
                        Intent intent4 = new Intent(CoordinatorDashboardActivity.this, AddSectionActivity.class);
                        intent4.putExtra("current_user_role", "Coordinator");
                        intent4.putExtra("programme_id", currentCoordinator.getProgrammeID());
                        startActivity(intent4);
                        break;
                }
                if (position != 0) binding.spinnerAdd.setSelection(0);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupNavigation() {
        binding.btnMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));

        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already here
            } else if (id == R.id.nav_add_lecturer) {
                Intent intent = new Intent(this, AddLecturerActivity.class);
                intent.putExtra("current_user_role", "Coordinator");
                intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
                startActivity(intent);
            } else if (id == R.id.nav_add_course) {
                Intent intent = new Intent(this, AddCourseActivity.class);
                intent.putExtra("current_user_role", "Coordinator");
                intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
                startActivity(intent);
            } else if (id == R.id.nav_add_section) {
                Intent intent = new Intent(this, AddSectionActivity.class);
                intent.putExtra("current_user_role", "Coordinator");
                intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
                startActivity(intent);
            } else if (id == R.id.nav_add_assignment) {
                Intent intent = new Intent(this, AddAssignmentActivity.class);
                intent.putExtra("current_user_role", "Coordinator");
                intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
                startActivity(intent);
            } else if (id == R.id.nav_my_lecturers) {
                Intent intent = new Intent(this, ManageLecturersActivity.class);
                intent.putExtra("current_user_role", "Coordinator");
                intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
                startActivity(intent);
            } else if (id == R.id.nav_my_courses) {
                Intent intent = new Intent(this, ManageCoursesActivity.class);
                intent.putExtra("current_user_role", "Coordinator");
                intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
                startActivity(intent);
            } else if (id == R.id.nav_manage_sections) {
                Intent intent = new Intent(this, ManageSectionsActivity.class);
                intent.putExtra("current_user_role", "Coordinator");
                intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
                startActivity(intent);
            } else if (id == R.id.nav_manage_assignments) {
                Intent intent = new Intent(this, ManageAssignmentsActivity.class);
                intent.putExtra("current_user_role", "Coordinator");
                intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
                startActivity(intent);
            } else if (id == R.id.nav_manage_workload) {
                Intent intent = new Intent(this, WorkloadCalculationActivity.class);
                intent.putExtra("current_user_role", "Coordinator");
                intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
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
        binding.cardViewLecturers.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageLecturersActivity.class);
            intent.putExtra("current_user_role", "Coordinator");
            intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
            startActivity(intent);
        });
        binding.cardViewCourses.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageCoursesActivity.class);
            intent.putExtra("current_user_role", "Coordinator");
            intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
            startActivity(intent);
        });
        binding.cardViewSections.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageSectionsActivity.class);
            intent.putExtra("current_user_role", "Coordinator");
            intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
            startActivity(intent);
        });
        binding.cardViewAssignments.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageAssignmentsActivity.class);
            intent.putExtra("current_user_role", "Coordinator");
            intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
            startActivity(intent);
        });
        binding.cardViewWorkload.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkloadCalculationActivity.class);
            intent.putExtra("current_user_role", "Coordinator");
            intent.putExtra("programme_id", currentCoordinator.getProgrammeID());
            startActivity(intent);
        });
    }
}
