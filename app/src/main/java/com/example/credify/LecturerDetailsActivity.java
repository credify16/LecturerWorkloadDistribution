package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.credify.data.model.Lecturer;
import com.example.credify.databinding.ActivityLecturerDetailsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class LecturerDetailsActivity extends AppCompatActivity {

    private ActivityLecturerDetailsBinding binding;
    private Lecturer currentLecturer;
    private boolean isReadOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLecturerDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lecturer Details");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 2. Get Data from Intent
        currentLecturer = getIntent().getParcelableExtra("lecturer_data");
        isReadOnly = getIntent().getBooleanExtra("is_read_only", false);

        if (currentLecturer == null) {
            // Fallback for old way or missing data
            String id = getIntent().getStringExtra("lecturer_id");
            if (id != null) {
                currentLecturer = new Lecturer();
                currentLecturer.setLecturerID(id);
                currentLecturer.setLecturerName(getIntent().getStringExtra("lecturer_name"));
                currentLecturer.setEmploymentType(getIntent().getStringExtra("employment_type"));
                currentLecturer.setDepartmentID(getIntent().getStringExtra("department_id"));
                currentLecturer.setPosition(getIntent().getStringExtra("position_id"));
            }
        }

        setupViewPager();

        // 3. Edit Button Logic
        binding.btnEdit.setVisibility(isReadOnly ? View.GONE : View.VISIBLE);
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditLecturerActivity.class);
            intent.putExtra("lecturer_data", currentLecturer);
            intent.putExtra("current_user_role", getIntent().getStringExtra("current_user_role"));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadLecturerData();
    }

    private void reloadLecturerData() {
        if (currentLecturer != null && currentLecturer.getLecturerID() != null) {
            new com.example.credify.data.repository.LecturerRepository()
                .getLecturerById(currentLecturer.getLecturerID())
                .thenAccept(updated -> {
                    if (updated != null) {
                        runOnUiThread(() -> {
                            currentLecturer = updated;
                            setupViewPager();
                        });
                    }
                });
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Profile" : "Work");
        }).attach();
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return LecturerProfileFragment.newInstance(currentLecturer, isReadOnly);
            } else {
                return LecturerOrgFragment.newInstance(
                        currentLecturer.getPosition(),
                        currentLecturer.getProgrammeID(),
                        currentLecturer.getDepartmentID()
                );
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}