package com.example.credify;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.credify.data.model.AssignmentDetail;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ActivityLecturerCreditDetailsBinding;
import com.example.credify.utils.WorkloadCalculator;
import com.example.credify.viewmodel.WorkloadViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LecturerCreditDetailsActivity extends AppCompatActivity {

    private ActivityLecturerCreditDetailsBinding binding;
    private WorkloadViewModel viewModel;
    private CourseAdapter adapter;
    private String lecturerName;
    private Lecturer lecturer;
    private List<AssignmentDetail> allDetails = new ArrayList<>();
    private List<SemesterSession> allSessions = new ArrayList<>();
    private List<SpinnerOption> spinnerOptions = new ArrayList<>();
    private boolean isReadOnly = false;

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
        binding = ActivityLecturerCreditDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WorkloadViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        isReadOnly = getIntent().getBooleanExtra("is_read_only", false);
        if (isReadOnly) {
            binding.layoutActionButtons.setVisibility(View.GONE);
        }

        lecturer = getIntent().getParcelableExtra("lecturer_data");
        lecturerName = getIntent().getStringExtra("LECTURER_NAME");
        if (lecturerName != null) {
            binding.tvLecturerName.setText(lecturerName);
        } else if (lecturer != null) {
            lecturerName = lecturer.getLecturerName();
            binding.tvLecturerName.setText(lecturerName);
        }

        binding.spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplay();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup RecyclerView for assigned courses
        binding.rvAssignedCourses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter(new ArrayList<>());
        binding.rvAssignedCourses.setAdapter(adapter);

        observeViewModel();
        viewModel.fetchSemesterSessions();
        viewModel.fetchAllDetails();
        
        // ... (rest of onCreate links)
        binding.btnViewBtsa.setOnClickListener(v -> {
            Intent intent = new Intent(this, BtsaCalculationActivity.class);
            intent.putExtra("LECTURER_ID", (lecturer != null) ? lecturer.getLecturerID() : "");
            intent.putExtra("LECTURER_NAME", lecturerName);
            intent.putExtra("is_read_only", isReadOnly);
            startActivity(intent);
        });

        binding.btnEditLecturer.setOnClickListener(v -> {
            if (lecturer != null) {
                Intent intent = new Intent(this, EditLecturerActivity.class);
                intent.putExtra("lecturer_data", lecturer);
                intent.putExtra("current_user_role", getIntent().getStringExtra("current_user_role"));
                startActivity(intent);
            }
        });

        binding.btnAddAssignment.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAssignmentActivity.class);
            if (lecturer != null) {
                intent.putExtra("prefill_lecturer_id", lecturer.getLecturerID());
                intent.putExtra("programme_id", lecturer.getProgrammeID());
            }
            intent.putExtra("current_user_role", getIntent().getStringExtra("current_user_role"));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLecturerData();
        if (viewModel != null) {
            viewModel.fetchAllDetails();
        }
    }

    private void refreshLecturerData() {
        String lid = (lecturer != null) ? lecturer.getLecturerID() : getIntent().getStringExtra("LECTURER_ID");
        
        if (lid != null && !lid.isEmpty()) {
            new com.example.credify.data.repository.LecturerRepository()
                .getLecturerById(lid)
                .thenAccept(updated -> {
                    if (updated != null) {
                        runOnUiThread(() -> {
                            lecturer = updated;
                            // Values displayed in UI should be semester targets
                            lecturerName = updated.getLecturerName();
                            binding.tvLecturerName.setText(lecturerName);
                            filterAndDisplay();
                        });
                    }
                });
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

            ArrayAdapter<SpinnerOption> adapterS = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerOptions);
            adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerSemester.setAdapter(adapterS);
        });

        viewModel.getAssignmentDetails().observe(this, details -> {
            this.allDetails = details;
            filterAndDisplay();
        });
    }

    private void filterAndDisplay() {
        SpinnerOption selected = (SpinnerOption) binding.spinnerSemester.getSelectedItem();
        if (selected == null) return;

        List<AssignedCourse> list = new ArrayList<>();
        double totalCredits = 0.0;
        
        String targetLid = (lecturer != null) ? lecturer.getLecturerID() : getIntent().getStringExtra("LECTURER_ID");

        for (AssignmentDetail detail : allDetails) {
            if (detail == null || detail.getLecturer() == null) continue;

            boolean lecturerMatch = false;
            if (targetLid != null && !targetLid.isEmpty()) {
                if (targetLid.equals(detail.getLecturer().getLecturerID())) {
                    lecturerMatch = true;
                }
            } else if (lecturerName != null && lecturerName.equalsIgnoreCase(detail.getLecturer().getLecturerName())) {
                // Fallback to name ONLY if ID is absolutely not available
                lecturerMatch = true;
            }

            if (lecturerMatch) {
                // Apply Filter
                if ("SESSION".equals(selected.type)) {
                    if (detail.getSemesterSession() == null || !selected.value.equals(detail.getSemesterSession().getSession())) {
                        continue;
                    }
                } else if ("SEMESTER".equals(selected.type)) {
                    if (detail.getSemesterSession() == null || !selected.value.equals(detail.getSemesterSession().getSemSessionID())) {
                        continue;
                    }
                }

                // Skip Part-time assignments from calculation
                if (detail.getAssignment() != null && "Part-time".equalsIgnoreCase(detail.getAssignment().getType())) {
                    // We still add to list for display, but credits = 0 for summary total
                    String courseName = (detail.getCourse() != null) ? detail.getCourse().getCourseName() : "Unknown Course";
                    String courseCode = (detail.getCourse() != null) ? detail.getCourse().getCourseCode() : (detail.getAssignment() != null ? detail.getAssignment().getCourseCode() : "N/A");
                    String sectionNum = (detail.getSection() != null) ? detail.getSection().getSectionNumber() : "??";
                    String courseInfo = courseCode + " " + courseName + " - Section " + sectionNum + " (PT)";
                    list.add(new AssignedCourse(courseInfo, "0.00 (PT)"));
                    continue;
                }

                // Centralized Credit Calculation
                double credits = WorkloadCalculator.calculateCourseCredits(
                        detail.getCourse(),
                        detail.getAssignment() != null ? detail.getAssignment().getLoadPercentage() : 100.0
                );
                
                totalCredits += credits;
                
                String courseName = (detail.getCourse() != null) ? detail.getCourse().getCourseName() : "Unknown Course";
                String courseCode = "N/A";
                if (detail.getCourse() != null) {
                    courseCode = detail.getCourse().getCourseCode();
                } else if (detail.getAssignment() != null) {
                    courseCode = detail.getAssignment().getCourseCode();
                }

                String sectionNum = (detail.getSection() != null) ? detail.getSection().getSectionNumber() : "??";
                
                String courseInfo = courseCode + " " + courseName + " - Section " + sectionNum;
                list.add(new AssignedCourse(courseInfo, String.format(Locale.getDefault(), "%.2f", credits)));
            }
        }

        adapter.updateList(list);
        updateSummary(totalCredits);
    }

    private void updateSummary(double total) {
        SpinnerOption selected = (SpinnerOption) binding.spinnerSemester.getSelectedItem();
        boolean isAnnual = selected != null && "SESSION".equals(selected.type);

        binding.tvTargetLabel.setText(isAnnual ? "Annual Target:" : "Semester Target:");
        double norm = WorkloadCalculator.getTargetCredit(lecturer);
        double diff = total - norm;

        binding.tvNormKredit.setText(String.format(Locale.getDefault(), "%.2f", norm));
        binding.tvTotalKredit.setText(String.format(Locale.getDefault(), "%.2f", total));
        
        String diffStr = String.format(Locale.getDefault(), "%s%.2f", (diff > 0 ? "+" : ""), diff);
        binding.tvDiffKredit.setText(diffStr);
        
        int percentage = norm > 0 ? (int) ((total / norm) * 100) : 0;
        
        // New Design: Semester view is informational only. Evaluation layer only in Annual view.
        int evalVisibility = isAnnual ? View.VISIBLE : View.GONE;
        
        binding.layoutTargetRow.setVisibility(evalVisibility);
        binding.dividerTarget.setVisibility(evalVisibility);
        binding.layoutDiffRow.setVisibility(evalVisibility);
        binding.dividerDiff.setVisibility(evalVisibility);

        // Percentage/Progress in header
        binding.pbCreditLoad.setVisibility(evalVisibility);
        binding.tvCreditPercentage.setVisibility(evalVisibility);
        
        // Adjust ProgressBar if visible
        if (isAnnual) {
            binding.pbCreditLoad.setProgress(Math.min(percentage, 100));
            binding.tvCreditPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));

            // Evaluation Color Coding
            int color;
            if (percentage < 90) {
                color = Color.parseColor("#FBC02D"); // Yellow - Under
            } else if (percentage > 110) {
                color = ContextCompat.getColor(this, android.R.color.holo_red_dark); // Red - Overload
            } else {
                color = ContextCompat.getColor(this, android.R.color.holo_green_dark); // Green - Good
            }
            binding.tvCreditPercentage.setTextColor(color);
            binding.pbCreditLoad.setProgressTintList(ColorStateList.valueOf(color));
        }
    }

    static class AssignedCourse {
        String name;
        String credits;
        AssignedCourse(String name, String credits) {
            this.name = name;
            this.credits = credits;
        }
    }

    static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
        private List<AssignedCourse> list;

        CourseAdapter(List<AssignedCourse> list) {
            this.list = list;
        }

        void updateList(List<AssignedCourse> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assigned_course, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AssignedCourse item = list.get(position);
            holder.tvName.setText(item.name);
            holder.tvCredits.setText(item.credits);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCredits;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvCourseName);
                tvCredits = v.findViewById(R.id.tvCourseCredits);
            }
        }
    }
}
