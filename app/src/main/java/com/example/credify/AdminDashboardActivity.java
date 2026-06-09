package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.credify.data.model.Admin;
import com.example.credify.databinding.AdminDashboardBinding;

public class AdminDashboardActivity extends AppCompatActivity {

    private AdminDashboardBinding binding;
    private Admin currentAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = AdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fetchProfile();

        // 1. Setup Spinner (ADD :)
        String[] addOptions = {
                "Select Action",
                "Add Admin",
                "Add Assignment",
                "Add Course",
                "Add Lecturer",
                "Add Programme",
                "Add Section",
                "Add Semester Session"

        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                addOptions
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAdd.setAdapter(adapter);

        binding.spinnerAdd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {

                    case 1:
                        startActivity(new Intent(
                                AdminDashboardActivity.this,
                                AddAdminActivity.class
                        ));
                        break;

                    case 2:
                        startActivity(new Intent(
                                AdminDashboardActivity.this,
                                AddAssignmentActivity.class
                        ));
                        break;

                    case 3:
                        startActivity(new Intent(
                                AdminDashboardActivity.this,
                                AddCourseActivity.class
                        ));
                        break;

                    case 4:
                        Intent intent = new Intent(AdminDashboardActivity.this, AddLecturerActivity.class);
                        intent.putExtra("current_user_role", "Admin");
                        startActivity(intent);
                        break;

                    case 5:
                        startActivity(new Intent(
                                AdminDashboardActivity.this,
                                AddProgrammeActivity.class
                        ));
                        break;

                    case 6:
                        startActivity(new Intent(
                                AdminDashboardActivity.this,
                                AddSectionActivity.class
                        ));
                        break;
                    case 7:
                        startActivity(new Intent(
                                AdminDashboardActivity.this,
                                AddSemesterSessionActivity.class
                        ));
                        break;
                }

                if (position != 0) {
                    binding.spinnerAdd.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 2. Setup Menu Button to open Drawer
        binding.btnMenu.setOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        binding.navView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            // Dashboard
            if (id == R.id.nav_dashboard) {

                binding.drawerLayout.closeDrawer(GravityCompat.START);

            } else if (id == R.id.nav_profile) {

                if (currentAdmin != null) {
                    Intent intent = new Intent(this, EditAdminActivity.class);
                    intent.putExtra("admin_id", currentAdmin.getAdminID());
                    intent.putExtra("admin_name", currentAdmin.getAdminName());
                    intent.putExtra("admin_email", currentAdmin.getEmail());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Profile not loaded yet", Toast.LENGTH_SHORT).show();
                }

            }

            // ADD MANAGEMENT
            else if (id == R.id.nav_add_admin) {

                startActivity(new Intent(this, AddAdminActivity.class));

            } else if (id == R.id.nav_add_lecturer) {
                Intent intent = new Intent(this, AddLecturerActivity.class);
                intent.putExtra("current_user_role", "Admin");
                startActivity(intent);

            } else if (id == R.id.nav_add_programme) {
                startActivity(new Intent(this, AddProgrammeActivity.class));

            } else if (id == R.id.nav_add_course) {

                startActivity(new Intent(this, AddCourseActivity.class));

            } else if (id == R.id.nav_add_section) {

                startActivity(new Intent(this, AddSectionActivity.class));

            } else if (id == R.id.nav_add_assignment) {

                startActivity(new Intent(this, AddAssignmentActivity.class));

            } else if (id == R.id.nav_add_semester) {

                startActivity(new Intent(this, AddSemesterSessionActivity.class));

            }

            // VIEW RECORDS
            else if (id == R.id.nav_view_admin) {

                startActivity(new Intent(this, ManageAdminsActivity.class));

            } else if (id == R.id.nav_view_lecturer) {
                Intent intent = new Intent(this, ManageLecturersActivity.class);
                intent.putExtra("current_user_role", "Admin");
                startActivity(intent);

            } else if (id == R.id.nav_view_programme) {
                startActivity(new Intent(this, ManageProgrammesActivity.class));

            } else if (id == R.id.nav_view_course) {
                Intent intent = new Intent(this, ManageCoursesActivity.class);
                intent.putExtra("current_user_role", "Admin");
                startActivity(intent);
            } else if (id == R.id.nav_view_section) {
                Intent intent = new Intent(this, ManageSectionsActivity.class);
                intent.putExtra("current_user_role", "Admin");
                startActivity(intent);

            } else if (id == R.id.nav_view_assignment) {

                startActivity(new Intent(this, ManageAssignmentsActivity.class));

            } else if (id == R.id.nav_view_semester) {

                startActivity(new Intent(this, ManageSemesterSessionsActivity.class));

            }

            // LOGOUT
            else if (id == R.id.nav_logout) {

                binding.btnLogout.performClick();
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        });

        // 3. Setup Logout Button
        binding.btnLogout.setOnClickListener(v -> {

            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

            new com.example.credify.data.repository.UserRepository().logout();

            startActivity(new Intent(this, LoginActivity.class));

            finish();
        });

        // 4. Card Click Listeners

        binding.cardViewAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageAdminsActivity.class);
            intent.putExtra("current_user_role", "Admin");
            startActivity(intent);
        });

        binding.cardViewLecturer.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageLecturersActivity.class);
            intent.putExtra("current_user_role", "Admin");
            startActivity(intent);
        });

        binding.cardViewProgramme.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageProgrammesActivity.class));
        });

        binding.cardViewCourse.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageCoursesActivity.class);
            intent.putExtra("current_user_role", "Admin");
            startActivity(intent);
        });

        binding.cardViewSection.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageSectionsActivity.class);
            intent.putExtra("current_user_role", "Admin");
            startActivity(intent);
        });

        binding.cardViewAssignment.setOnClickListener(v -> {
            startActivity(new Intent(this, ManageAssignmentsActivity.class));
        });

        // Semester Session
        binding.cardViewSemester.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    ManageSemesterSessionsActivity.class
            ));
        });
    }

    private void fetchProfile() {
        new com.example.credify.data.repository.UserRepository().detectRole().thenAccept(profile -> {
            if (profile instanceof Admin) {
                this.currentAdmin = (Admin) profile;
            }
        });
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
}
