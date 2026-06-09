package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.lifecycle.ViewModelProvider;
import com.example.credify.data.model.Assignment;
import com.example.credify.databinding.ActivityAssignmentDetailsBinding;
import com.example.credify.viewmodel.AssignmentDetailViewModel;
import com.google.android.material.tabs.TabLayoutMediator;

public class AssignmentDetailsActivity extends AppCompatActivity {

    private ActivityAssignmentDetailsBinding binding;
    private AssignmentDetailViewModel detailViewModel;
    
    private String assignmentId;
    private boolean isReadOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAssignmentDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        detailViewModel = new ViewModelProvider(this).get(AssignmentDetailViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Assignment Details");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get assignment data
        Assignment assignment = getIntent().getParcelableExtra("assignment_data");
        isReadOnly = getIntent().getBooleanExtra("is_read_only", false);

        if (assignment != null) {
            assignmentId = assignment.getAssignmentID();
        }

        setupViewPager();

        binding.btnEdit.setVisibility(isReadOnly ? android.view.View.GONE : android.view.View.VISIBLE);
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditAssignmentActivity.class);
            intent.putExtra("assignment_data", detailViewModel.getAssignmentDetail().getValue() != null ? 
                detailViewModel.getAssignmentDetail().getValue().getAssignment() : assignment);
            intent.putExtra("current_user_role", getIntent().getStringExtra("current_user_role"));
            intent.putExtra("programme_id", getIntent().getStringExtra("programme_id"));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (assignmentId != null) {
            detailViewModel.loadAssignment(assignmentId);
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Assignment Info" : "Lecturer Info");
        }).attach();
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return AssignmentInfoFragment.newInstance();
            } else {
                return LecturerInfoFragment.newInstance();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
