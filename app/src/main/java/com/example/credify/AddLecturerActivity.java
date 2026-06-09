package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.credify.data.model.Department;
import com.example.credify.data.model.Lecturer;
import com.example.credify.databinding.ActivityAddLecturerBinding;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.LecturerState;
import com.example.credify.viewmodel.LecturerViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddLecturerActivity extends AppCompatActivity {

    private ActivityAddLecturerBinding binding;
    private LecturerViewModel viewModel;

    private String[] roles = {"Course Coordinator", "Lecturer"};
    private final String[] employmentTypes = {"Full-time", "Part-time"};
    private final String[] positions = {
            "Ketua Jabatan",
            "Ketua Program / Penyelaras PPK",
            "Pengurus Besar",
            "CEO Kolej Space",
            "Penyelaras PPSM",
            "Penyelaras LI",
            "Pengerusi Kelab",
            "Lain-Lain"
    };
    private List<Department> departmentList = new ArrayList<>();
    private List<com.example.credify.data.model.Programme> programmeList = new ArrayList<>();
    private String currentUserRole;
    private String filteredProgrammeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddLecturerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserRole = getIntent().getStringExtra("current_user_role");
        filteredProgrammeId = getIntent().getStringExtra("programme_id");

        viewModel = new ViewModelProvider(this).get(LecturerViewModel.class);

        setupToolbar();
        setupSpinners();
        observeViewModel();

        binding.btnSave.setOnClickListener(v -> saveLecturer());

        viewModel.fetchDepartments();
        if (filteredProgrammeId != null && !filteredProgrammeId.isEmpty()) {
            // Coordinator: Hide programme selection, use their programme
            binding.tvProgrammeLabel.setVisibility(View.GONE);
            binding.spinnerProgramme.setVisibility(View.GONE);
        } else if ("Admin".equalsIgnoreCase(currentUserRole)) {
            // Admin: Show programme selection
            binding.tvProgrammeLabel.setVisibility(View.VISIBLE);
            binding.spinnerProgramme.setVisibility(View.VISIBLE);
            viewModel.fetchProgrammes();
        } else if ("Coordinator".equalsIgnoreCase(currentUserRole)) {
            // Fallback if programme_id was NOT passed but role IS coordinator (shouldn't happen with my changes)
            binding.tvProgrammeLabel.setVisibility(View.VISIBLE);
            binding.spinnerProgramme.setVisibility(View.VISIBLE);
            viewModel.fetchProgrammes();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Lecturer");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        if ("Coordinator".equalsIgnoreCase(currentUserRole)) {
            roles = new String[]{"Lecturer"};
        }
        
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRole.setAdapter(roleAdapter);

        ArrayAdapter<String> empAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, employmentTypes);
        empAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEmploymentType.setAdapter(empAdapter);

        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, positions);
        binding.etPosition.setAdapter(positionAdapter);
    }

    private void observeViewModel() {
        viewModel.getDepartments().observe(this, depts -> {
            this.departmentList = depts;
            List<String> names = new ArrayList<>();
            for (Department d : depts) names.add(d.getDepartmentName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerDepartment.setAdapter(adapter);
        });

        viewModel.getProgrammes().observe(this, progs -> {
            this.programmeList = progs;
            List<String> names = new ArrayList<>();
            for (com.example.credify.data.model.Programme p : progs) names.add(p.getProgrammeName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerProgramme.setAdapter(adapter);
        });

        viewModel.getLecturerState().observe(this, state -> {
            if (state instanceof LecturerState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSave.setEnabled(false);
            } else if (state instanceof LecturerState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                DialogUtils.showSuccessDialog(this, "SUCCESSFULLY ADDED", 
                    ((LecturerState.ActionSuccess) state).getMessage(), 
                    this::finish);
            } else if (state instanceof LecturerState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(this, "Error: " + ((LecturerState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLecturer() {
        String id = binding.etLecturerId.getText().toString().trim();
        String name = binding.etLecturerName.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String position = binding.etPosition.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String btsaStr = binding.etNormalBTSA.getText().toString().trim();
        String creditStr = binding.etNormalCredit.getText().toString().trim();

        int empPos = binding.spinnerEmploymentType.getSelectedItemPosition();
        int deptPos = binding.spinnerDepartment.getSelectedItemPosition();

        if (id.isEmpty() || name.isEmpty() || password.isEmpty() || deptPos < 0) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Lecturer l = new Lecturer();
        l.setLecturerID(id);
        l.setLecturerName(name);
        l.setPassword(password);
        l.setLecturerRole(binding.spinnerRole.getSelectedItem().toString());
        l.setPosition(position);
        l.setEmploymentType(employmentTypes[empPos]);
        l.setDepartmentID(departmentList.get(deptPos).getDepartmentID());
        l.setEmail(email);

        if (filteredProgrammeId != null && !filteredProgrammeId.isEmpty()) {
            l.setProgrammeID(filteredProgrammeId);
        } else if ("Admin".equalsIgnoreCase(currentUserRole) || "Coordinator".equalsIgnoreCase(currentUserRole)) {
            int progPos = binding.spinnerProgramme.getSelectedItemPosition();
            if (progPos >= 0 && progPos < programmeList.size()) {
                l.setProgrammeID(programmeList.get(progPos).getProgrammeID());
            }
        }

        try {
            l.setNormalBTSA(btsaStr.isEmpty() ? 0.0 : Double.parseDouble(btsaStr));
        } catch (NumberFormatException e) {
            l.setNormalBTSA(0.0);
        }

        try {
            l.setNormalCredit(creditStr.isEmpty() ? 0.0 : Double.parseDouble(creditStr));
        } catch (NumberFormatException e) {
            l.setNormalCredit(0.0);
        }

        viewModel.addLecturer(l);
    }
}
