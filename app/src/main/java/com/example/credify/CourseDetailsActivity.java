package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.credify.data.model.Course;
import com.example.credify.databinding.ActivityCourseDetailsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class CourseDetailsActivity extends AppCompatActivity {

    private ActivityCourseDetailsBinding binding;
    private Course course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourseDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Course Details");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        course = getIntent().getParcelableExtra("course_data");

        if (course == null) {
            Toast.makeText(this, "Error: No course data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViewPager();

        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditCourseActivity.class);
            intent.putExtra("course_data", course);
            intent.putExtra("current_user_role", getIntent().getStringExtra("current_user_role"));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadCourseData();
    }

    private void reloadCourseData() {
        if (course != null && course.getCourseCode() != null) {
            new com.example.credify.data.repository.CourseRepository()
                .getCourseByCode(course.getCourseCode())
                .thenAccept(updated -> {
                    if (updated != null) {
                        runOnUiThread(() -> {
                            course = updated;
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
            tab.setText(position == 0 ? "General" : "Values");
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
                return CourseGeneralFragment.newInstance(course.getCourseCode(), course.getCourseName(), course.getMethod(), course.getProgrammeID());
            } else {
                return CourseValueFragment.newInstance(course.getWeeklyHour(), course.getCreditValue());
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
