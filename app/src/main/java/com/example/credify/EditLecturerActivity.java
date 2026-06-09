package com.example.credify;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.credify.data.model.Department;
import com.example.credify.data.model.Lecturer;
import com.example.credify.databinding.ActivityEditLecturerBinding;
import com.example.credify.utils.DialogUtils;
import com.example.credify.viewmodel.LecturerState;
import com.example.credify.viewmodel.LecturerViewModel;

import java.util.ArrayList;
import java.util.List;

public class EditLecturerActivity extends AppCompatActivity {

    private ActivityEditLecturerBinding binding;
    private LecturerViewModel viewModel;
    
    private Lecturer currentLecturer;
    private final String[] roles = {"Course Coordinator", "Lecturer"};
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditLecturerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserRole = getIntent().getStringExtra("current_user_role");

        viewModel = new ViewModelProvider(this).get(LecturerViewModel.class);

        // 1. Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Lecturer");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 2. Get Data from Intent
        currentLecturer = getIntent().getParcelableExtra("lecturer_data");

        setupSpinners();
        observeViewModel();

        if (currentLecturer != null) {
            populateFields();
        } else {
            Toast.makeText(this, "Error: No lecturer data found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 3. Update Button Logic
        binding.btnUpdate.setOnClickListener(v -> updateLecturer());
        
        viewModel.fetchDepartments();
        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            binding.tvProgrammeLabel.setVisibility(View.VISIBLE);
            binding.spinnerProgramme.setVisibility(View.VISIBLE);
            viewModel.fetchProgrammes();
        } else if ("Coordinator".equalsIgnoreCase(currentUserRole)) {
            // Coordinator cannot change programme
            binding.tvProgrammeLabel.setVisibility(View.GONE);
            binding.spinnerProgramme.setVisibility(View.GONE);
        }
    }

    private void setupSpinners() {
        if ("Coordinator".equalsIgnoreCase(currentUserRole)) {
            binding.spinnerRole.setEnabled(false);
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

    private void populateFields() {
        binding.etLecturerId.setText(currentLecturer.getLecturerID());
        binding.etLecturerId.setEnabled(false); // ID should usually be immutable
        binding.etLecturerName.setText(currentLecturer.getLecturerName());
        binding.etPosition.setText(currentLecturer.getPosition());
        binding.etEmail.setText(currentLecturer.getEmail());
        binding.etNormalBTSA.setText(String.valueOf(currentLecturer.getNormalBTSA() != null ? currentLecturer.getNormalBTSA() : 0.0));
        binding.etNormalCredit.setText(String.valueOf(currentLecturer.getNormalCredit() != null ? currentLecturer.getNormalCredit() : 0.0));

        // Set spinner selections
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(currentLecturer.getLecturerRole())) {
                binding.spinnerRole.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < employmentTypes.length; i++) {
            if (employmentTypes[i].equals(currentLecturer.getEmploymentType())) {
                binding.spinnerEmploymentType.setSelection(i);
                break;
            }
        }
    }

    private void observeViewModel() {
        viewModel.getDepartments().observe(this, depts -> {
            this.departmentList = depts;
            List<String> names = new ArrayList<>();
            for (Department d : depts) names.add(d.getDepartmentName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerDepartment.setAdapter(adapter);

            // Set department selection if currentLecturer is not null
            if (currentLecturer != null) {
                for (int i = 0; i < depts.size(); i++) {
                    if (depts.get(i).getDepartmentID().equals(currentLecturer.getDepartmentID())) {
                        binding.spinnerDepartment.setSelection(i);
                        break;
                    }
                }
            }
        });

        viewModel.getProgrammes().observe(this, progs -> {
            this.programmeList = progs;
            List<String> names = new ArrayList<>();
            for (com.example.credify.data.model.Programme p : progs) names.add(p.getProgrammeName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerProgramme.setAdapter(adapter);

            if (currentLecturer != null && currentLecturer.getProgrammeID() != null) {
                for (int i = 0; i < progs.size(); i++) {
                    if (progs.get(i).getProgrammeID().equals(currentLecturer.getProgrammeID())) {
                        binding.spinnerProgramme.setSelection(i);
                        break;
                    }
                }
            }
        });

        viewModel.getLecturerState().observe(this, state -> {
            if (state instanceof LecturerState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnUpdate.setEnabled(false);
            } else if (state instanceof LecturerState.ActionSuccess) {
                binding.progressBar.setVisibility(View.GONE);
                DialogUtils.showSuccessDialog(this, "SUCCESSFULLY UPDATED", 
                    ((LecturerState.ActionSuccess) state).getMessage(), 
                    this::finish);
            } else if (state instanceof LecturerState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnUpdate.setEnabled(true);
                Toast.makeText(this, "Error: " + ((LecturerState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLecturer() {
        String name = binding.etLecturerName.getText().toString().trim();
        String position = binding.etPosition.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String btsaStr = binding.etNormalBTSA.getText().toString().trim();
        String creditStr = binding.etNormalCredit.getText().toString().trim();
        
        int empPos = binding.spinnerEmploymentType.getSelectedItemPosition();
        int deptPos = binding.spinnerDepartment.getSelectedItemPosition();

        if (name.isEmpty() || deptPos < 0) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Lecturer l = new Lecturer();
        l.setLecturerID(currentLecturer.getLecturerID());
        l.setLecturerName(name);
        l.setLecturerRole(binding.spinnerRole.getSelectedItem().toString());
        l.setPosition(position);
        l.setEmploymentType(employmentTypes[empPos]);
        l.setDepartmentID(departmentList.get(deptPos).getDepartmentID());
        l.setEmail(email);

        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            int progPos = binding.spinnerProgramme.getSelectedItemPosition();
            if (progPos >= 0 && progPos < programmeList.size()) {
                l.setProgrammeID(programmeList.get(progPos).getProgrammeID());
            } else {
                l.setProgrammeID(currentLecturer.getProgrammeID());
            }
        } else {
            // Coordinator or Lecturer: keep existing programme
            l.setProgrammeID(currentLecturer.getProgrammeID());
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

        viewModel.updateLecturer(l);
    }
}
