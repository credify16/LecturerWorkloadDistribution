package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.credify.data.model.Lecturer;
import com.example.credify.databinding.ActivityManageLecturersBinding;
import com.example.credify.ui.adapter.LecturerAdapter;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.LecturerState;
import com.example.credify.viewmodel.LecturerViewModel;

import java.util.ArrayList;

public class ManageLecturersActivity extends AppCompatActivity {

    private ActivityManageLecturersBinding binding;
    private LecturerViewModel viewModel;
    private LecturerAdapter adapter;
    private String currentUserRole;
    private String filteredProgrammeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageLecturersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserRole = getIntent().getStringExtra("current_user_role");
        filteredProgrammeId = getIntent().getStringExtra("programme_id");
        
        // If no programme_id passed explicitly, but user is Coordinator, 
        // they should still be restricted (usually filteredProgrammeId is passed from Dashboard)

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this)
                .get(LecturerViewModel.class);

        setupRecyclerView();
        setupSearch();
        observeViewModel();

        binding.btnAddLecturer.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddLecturerActivity.class);
            intent.putExtra("current_user_role", currentUserRole);
            intent.putExtra("programme_id", filteredProgrammeId);
            startActivity(intent);
        });

        loadData();
    }

    private void setupRecyclerView() {
        adapter = new LecturerAdapter(
                new ArrayList<>(),
                lecturer -> {
                    if ("Coordinator".equalsIgnoreCase(currentUserRole)) {
                        Intent intent = new Intent(this, LecturerCreditDetailsActivity.class);
                        intent.putExtra("lecturer_data", lecturer);
                        intent.putExtra("LECTURER_ID", lecturer.getLecturerID());
                        intent.putExtra("LECTURER_NAME", lecturer.getLecturerName());
                        intent.putExtra("current_user_role", currentUserRole);
                        startActivity(intent);
                    } else {
                        // If Admin, they edit everyone.
                        Intent intent = new Intent(this, EditLecturerActivity.class);
                        intent.putExtra("lecturer_data", lecturer);
                        intent.putExtra("current_user_role", currentUserRole);
                        startActivity(intent);
                    }
                },
                lecturer -> {
                    if ("Coordinator".equalsIgnoreCase(currentUserRole) && "Course Coordinator".equals(lecturer.getLecturerRole())) {
                        Toast.makeText(this, "You cannot delete a Course Coordinator.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (lecturer.getLecturerID() != null) {
                        showDeleteConfirmation(lecturer.getLecturerID(), lecturer.getLecturerName());
                    }
                }
        );
        binding.rvLecturers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLecturers.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void showDeleteConfirmation(String lecturerId, String lecturerName) {
        DialogUtils.showDeleteConfirmationDialog(this, 
            "Are you sure you want to delete lecturer:\n\n" + lecturerName + "? This action cannot be undone.",
            () -> viewModel.deleteLecturer(lecturerId));
    }

    private void observeViewModel() {
        viewModel.getLecturerState().observe(this, state -> {
            if (state instanceof LecturerState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
            } else if (state instanceof LecturerState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.GONE);
                adapter.updateLecturers(((LecturerState.Success) state).getLecturers());
            } else if (state instanceof LecturerState.Empty) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                adapter.updateLecturers(new ArrayList<>());
            } else if (state instanceof LecturerState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((LecturerState.ActionSuccess) state).getMessage(), Toast.LENGTH_SHORT).show();
                loadData();
            } else if (state instanceof LecturerState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ((LecturerState.Error) state).getMessage(), Toast.LENGTH_LONG).show();
                // Only show layoutEmpty if we truly have no data to show
                if (adapter.getItemCount() == 0) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    TextView tvEmpty = binding.layoutEmpty.findViewById(R.id.tvEmptyText);
                    if (tvEmpty != null) {
                        tvEmpty.setText(((LecturerState.Error) state).getMessage());
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        android.util.Log.d("ManageLecturers", "loadData: role=" + currentUserRole + ", progId=" + filteredProgrammeId);
        if (filteredProgrammeId != null && !filteredProgrammeId.isEmpty()) {
            viewModel.fetchLecturersByProgramme(filteredProgrammeId);
        } else {
            viewModel.fetchLecturers();
        }
    }
}
