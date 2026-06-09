package com.example.credify;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.credify.data.model.AssignmentDetail;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ActivityWorkloadManagementBinding;
import com.example.credify.utils.WorkloadCalculator;
import com.example.credify.viewmodel.WorkloadViewModel;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class WorkloadCalculationActivity extends AppCompatActivity {

    private ActivityWorkloadManagementBinding binding;
    private WorkloadViewModel viewModel;
    private List<AssignmentDetail> allDetails = new ArrayList<>();
    private List<SemesterSession> allSessions = new ArrayList<>();
    private List<SpinnerOption> spinnerOptions = new ArrayList<>();

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
        binding = ActivityWorkloadManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WorkloadViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Navigation to detailed breakdowns
        binding.btnViewBtsa.setOnClickListener(v -> startActivity(new Intent(this, BtsaCalculationActivity.class)));
        binding.btnViewCredit.setOnClickListener(v -> startActivity(new Intent(this, KreditCalculationActivity.class)));

        binding.spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplay();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.rvLecturers.setLayoutManager(new LinearLayoutManager(this));
        
        observeViewModel();
        viewModel.fetchSemesterSessions();
        viewModel.fetchAllDetails();

        binding.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAssignmentActivity.class);
            intent.putExtra("current_user_role", "Coordinator"); // Usually opened by coordinator
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.fetchAllDetails();
        }
    }

    private void observeViewModel() {
        viewModel.getSemesterSessions().observe(this, sessions -> {
            this.allSessions = sessions;
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

            // Default to "All" as requested
            binding.spinnerSemester.setSelection(0);
        });

        viewModel.getAssignmentDetails().observe(this, details -> {
            this.allDetails = details;
            filterAndDisplay();
        });
    }

    private void filterAndDisplay() {
        SpinnerOption selected = (SpinnerOption) binding.spinnerSemester.getSelectedItem();
        if (selected == null) return;
        
        Map<String, LecturerWorkload> map = new HashMap<>();
        double globalCredits = 0;
        double globalBtsa = 0;
        int lecturerCount = 0;

        for (AssignmentDetail detail : allDetails) {
            if (detail.getSemesterSession() == null) continue;
            
            // Filtering
            if ("SESSION".equals(selected.type)) {
                if (!selected.value.equals(detail.getSemesterSession().getSession())) {
                    continue;
                }
            } else if ("SEMESTER".equals(selected.type)) {
                if (!selected.value.equals(detail.getSemesterSession().getSemSessionID())) {
                    continue;
                }
            }
            // "ALL" case doesn't need additional check as it includes everyone

            Lecturer l = detail.getLecturer();
            if (l == null) continue;

            String lid = l.getLecturerID();
            LecturerWorkload lw = map.get(lid);
            if (lw == null) {
                LecturerWorkload newLw = new LecturerWorkload(l.getLecturerName());
                // Get institutional target (no scaling)
                newLw.normalCredits = WorkloadCalculator.getTargetCredit(l);
                newLw.normalBtsa = WorkloadCalculator.getTargetBTSA(l);
                newLw.employmentType = l.getEmploymentType();
                newLw.isEvaluationLayer = "SESSION".equals(selected.type);
                map.put(lid, newLw);
                lw = newLw;
                lecturerCount++;
            }
            
            if (detail.getCourse() != null && detail.getAssignment() != null) {
                // Skip Part-time assignments from calculation
                if ("Part-time".equalsIgnoreCase(detail.getAssignment().getType())) {
                    lw.assignmentDetails.add(detail);
                    continue;
                }

                // Centralized Credit Calculation
                double credits = WorkloadCalculator.calculateCourseCredits(
                        detail.getCourse(), 
                        detail.getAssignment().getLoadPercentage()
                );
                
                lw.totalCredits += credits;
                globalCredits += credits;

                // Centralized BTSA Calculation
                double btsa = WorkloadCalculator.calculateCourseBTSA(
                        detail.getCourse(),
                        detail.getSection(),
                        detail.getAssignment().getLoadPercentage()
                );
                lw.totalBtsa += btsa;
                globalBtsa += btsa;
                
                // Track details for warnings
                lw.assignmentDetails.add(detail);
            }
        }

        List<LecturerWorkload> list = new ArrayList<>(map.values());
        list.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        WorkloadAdapter adapter = new WorkloadAdapter(list);
        binding.rvLecturers.setAdapter(adapter);

        // Optional: Log or Toast the Summary Report as requested in PART 9 - OUTPUT 4
        String summary = String.format(Locale.getDefault(), 
            "Total Credits: %.1f | Total BTSA: %.1f\nAvg Credits: %.1f | Avg BTSA: %.1f | Lecturers: %d",
            globalCredits, globalBtsa, 
            lecturerCount > 0 ? globalCredits / lecturerCount : 0,
            lecturerCount > 0 ? globalBtsa / lecturerCount : 0,
            lecturerCount);
        android.util.Log.d("WorkloadSummary", summary);
    }

    static class LecturerWorkload {
        String name;
        String employmentType;
        double totalCredits = 0;
        double totalBtsa = 0;
        double normalCredits = 0.0;
        double normalBtsa = 0.0;
        boolean hasHighStudentWarning = false;
        boolean isEvaluationLayer = false;
        List<AssignmentDetail> assignmentDetails = new ArrayList<>();
        LecturerWorkload(String name) { this.name = name; }
    }

    static class WorkloadAdapter extends RecyclerView.Adapter<WorkloadAdapter.ViewHolder> {
        private List<LecturerWorkload> list;

        WorkloadAdapter(List<LecturerWorkload> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lecturer_beban, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LecturerWorkload item = list.get(position);
            holder.tvName.setText(item.name);
            
            double creditDiff = item.totalCredits - item.normalCredits;
            double btsaDiff = item.totalBtsa - item.normalBtsa;
            boolean isPartTime = "Part-Time".equalsIgnoreCase(item.employmentType);

            // 1. CREDIT DISPLAY
            if (item.isEvaluationLayer) {
                holder.tvOvertimeLabel.setText("Diff Credits :");
                holder.tvOvertime.setText(String.format(Locale.getDefault(), "%s%.1f", (creditDiff > 0.05 ? "+" : ""), creditDiff));
                if (isPartTime) {
                    holder.tvOvertime.setBackgroundResource(R.drawable.bg_oval_green);
                } else if (creditDiff > 0.05) {
                    holder.tvOvertime.setBackgroundResource(R.drawable.bg_oval_red);
                } else if (creditDiff < -0.05) {
                    holder.tvOvertime.setBackgroundResource(R.drawable.bg_oval_yellow);
                } else {
                    holder.tvOvertime.setBackgroundResource(R.drawable.bg_oval_green);
                }
            } else {
                holder.tvOvertimeLabel.setText("Credits :");
                holder.tvOvertime.setText(String.format(Locale.getDefault(), "%.1f", item.totalCredits));
                holder.tvOvertime.setBackgroundResource(R.drawable.bg_oval_green);
            }

            // 2. BTSA DISPLAY
            if (item.isEvaluationLayer) {
                holder.tvBtsaLabel.setText("Diff BTSA :");
                holder.tvBtsaValue.setText(String.format(Locale.getDefault(), "%s%.1f", (btsaDiff > 0.05 ? "+" : ""), btsaDiff));
                if (isPartTime) {
                    holder.tvBtsaValue.setBackgroundResource(R.drawable.bg_oval_green);
                } else if (btsaDiff > 0.05) {
                    holder.tvBtsaValue.setBackgroundResource(R.drawable.bg_oval_red);
                } else if (btsaDiff < -0.05) {
                    holder.tvBtsaValue.setBackgroundResource(R.drawable.bg_oval_yellow);
                } else {
                    holder.tvBtsaValue.setBackgroundResource(R.drawable.bg_oval_green);
                }
                holder.tvBtsaValue.setTextColor(Color.WHITE);
            } else {
                holder.tvBtsaLabel.setText("BTSA :");
                holder.tvBtsaValue.setText(String.format(Locale.getDefault(), "%.1f", item.totalBtsa));
                holder.tvBtsaValue.setBackgroundResource(R.drawable.bg_oval_green);
                holder.tvBtsaValue.setTextColor(Color.WHITE);
            }

            // Warnings using centralized calculator
            List<String> warningList = WorkloadCalculator.calculateWarnings(
                    item.totalBtsa, item.normalBtsa, item.totalCredits, item.normalCredits, item.employmentType, item.assignmentDetails, item.isEvaluationLayer
            );
            
            StringBuilder warnings = new StringBuilder();
            if (isPartTime) {
                warnings.append("[Part-Time Lecturer] ");
            }
            for (String w : warningList) {
                warnings.append(w).append(" ");
            }

            if (warnings.length() > 0) {
                holder.tvSubject.setVisibility(View.VISIBLE);
                holder.tvSubject.setText(warnings.toString().trim());
                holder.tvSubject.setTextColor(Color.RED);
            } else {
                holder.tvSubject.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvOvertime, tvSubject, tvOvertimeLabel, tvBtsaLabel, tvBtsaValue;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvLecturerName);
                tvOvertime = v.findViewById(R.id.tvOvertime);
                tvSubject = v.findViewById(R.id.tvSubject);
                tvOvertimeLabel = v.findViewById(R.id.tvOvertimeLabel);
                tvBtsaLabel = v.findViewById(R.id.tvBtsaLabel);
                tvBtsaValue = v.findViewById(R.id.tvBtsaValue);
            }
        }
    }
}
