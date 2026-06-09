package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.credify.databinding.ActivitySectionDetailsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class SectionDetailsActivity extends AppCompatActivity {

    private ActivitySectionDetailsBinding binding;
    private com.example.credify.data.model.Section currentSection;
    private boolean isReadOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySectionDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Section Details");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 2. Get Data from Intent
        currentSection = getIntent().getParcelableExtra("section_data");
        isReadOnly = getIntent().getBooleanExtra("is_read_only", false);

        if (currentSection == null) {
            Toast.makeText(this, "Error: No data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViewPager();

        // 3. Edit Button Logic
        binding.btnEdit.setVisibility(isReadOnly ? android.view.View.GONE : android.view.View.VISIBLE);
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditSectionActivity.class);
            intent.putExtra("section_data", currentSection);
            intent.putExtra("current_user_role", getIntent().getStringExtra("current_user_role"));
            startActivity(intent);
        });
    }

    private String campusName = "Loading...";
    private String semesterStr = "Loading...";
    private String sessionStr = "Loading...";

    @Override
    protected void onResume() {
        super.onResume();
        reloadSectionData();
    }

    private void reloadSectionData() {
        if (currentSection != null && currentSection.getSectionID() != null) {
            new com.example.credify.data.repository.SectionRepository()
                .getSectionById(currentSection.getSectionID())
                .thenAccept(section -> {
                    if (section != null) {
                        currentSection = section;
                        fetchRelatedData();
                    }
                });
        }
    }

    private void fetchRelatedData() {
        com.example.credify.data.repository.CampusRepository campusRepo = new com.example.credify.data.repository.CampusRepository();
        com.example.credify.data.repository.SemesterSessionRepository sessionRepo = new com.example.credify.data.repository.SemesterSessionRepository();

        campusRepo.getCampusNameById(currentSection.getCampusID()).thenAccept(name -> {
            campusName = name;
            checkAllDataLoaded();
        });

        sessionRepo.getSessionById(currentSection.getSemSessionID()).thenAccept(session -> {
            if (session != null) {
                semesterStr = session.getSemester();
                sessionStr = session.getSession();
            }
            checkAllDataLoaded();
        });
    }

    private int loadedCount = 0;
    private synchronized void checkAllDataLoaded() {
        loadedCount++;
        if (loadedCount >= 2) {
            loadedCount = 0;
            runOnUiThread(this::setupViewPager);
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "General" : "Details");
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
                return SectionGeneralFragment.newInstance(
                    currentSection.getSectionID(), 
                    currentSection.getSectionNumber(), 
                    semesterStr, 
                    sessionStr, 
                    campusName
                );
            } else {
                return SectionAdditionalFragment.newInstance(
                    currentSection.getProgrammeID(), 
                    currentSection.getStudentAmount() != null ? currentSection.getStudentAmount() : "0"
                );
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}