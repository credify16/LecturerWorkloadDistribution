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
import com.example.credify.databinding.ActivityCreditCalculationBinding;
import com.example.credify.utils.WorkloadCalculator;
import com.example.credify.viewmodel.WorkloadViewModel;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.credify.data.model.AssignmentDetail;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ActivityCreditCalculationBinding;
import com.example.credify.utils.WorkloadCalculator;
import com.example.credify.viewmodel.WorkloadViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class KreditCalculationActivity extends AppCompatActivity {

    private ActivityCreditCalculationBinding binding;
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
        binding = ActivityCreditCalculationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WorkloadViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUI();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.rvKreditBreakdown.setLayoutManager(new LinearLayoutManager(this));
        
        observeViewModel();
        viewModel.fetchSemesterSessions();
        viewModel.fetchAllDetails();

        binding.btnAddKredit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAssignmentActivity.class);
            intent.putExtra("current_user_role", "Coordinator");
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

            if (!sessions.isEmpty()) {
                binding.spinnerSemester.setSelection(0);
            }
        });

        viewModel.getAssignmentDetails().observe(this, details -> {
            this.allDetails = details;
            updateUI();
        });
    }

    private void updateUI() {
        SpinnerOption selected = (SpinnerOption) binding.spinnerSemester.getSelectedItem();
        if (selected == null) return;

        boolean isSummary = !"SEMESTER".equals(selected.type);
        
        if (isSummary) {
            binding.tvHeaderDetails.setText("Lecturer Name");
            binding.tvHeaderMetrics.setText("Workload Status");
        } else {
            binding.tvHeaderDetails.setText("Details (lect/subject/sec)");
            binding.tvHeaderMetrics.setText("Semester Credits");
        }

        loadKreditData(isSummary, selected);
    }

    private void loadKreditData(boolean isSummary, SpinnerOption selected) {
        List<LecturerKredit> list = new ArrayList<>();
        
        if (isSummary) {
            Map<String, LecturerKredit> map = new HashMap<>();
            for (AssignmentDetail detail : allDetails) {
                if (detail.getSemesterSession() == null) continue;

                // Filtering Logic
                if ("SESSION".equals(selected.type)) {
                    if (!selected.value.equals(detail.getSemesterSession().getSession())) {
                        continue;
                    }
                } else if ("SEMESTER".equals(selected.type)) {
                    // Although isSummary is false for SEMESTER, double check filtering
                    if (!selected.value.equals(detail.getSemesterSession().getSemSessionID())) {
                        continue;
                    }
                }

                Lecturer l = detail.getLecturer();
                if (l == null) continue;

                String lid = l.getLecturerID();
                LecturerKredit lk = map.get(lid);
                if (lk == null) {
                    LecturerKredit newLk = new LecturerKredit(l.getLecturerName(), "", "", "0.0", "0.0", true);
                    // Get institutional target (no scaling)
                    newLk.normalCredit = WorkloadCalculator.getTargetCredit(l);
                    newLk.lecturer = l;
                    map.put(lid, newLk);
                    lk = newLk;
                }
                
                if (detail.getCourse() != null && detail.getAssignment() != null) {
                    // Skip Part-time assignments from calculation
                    if ("Part-time".equalsIgnoreCase(detail.getAssignment().getType())) continue;

                    double credits = WorkloadCalculator.calculateCourseCredits(
                            detail.getCourse(),
                            detail.getAssignment().getLoadPercentage()
                    );
                    
                    double currentVal = Double.parseDouble(lk.kreditVal);
                    lk.kreditVal = String.format(Locale.getDefault(), "%.1f", currentVal + credits);
                }
            }
            
            for (LecturerKredit lk : map.values()) {
                double total = Double.parseDouble(lk.kreditVal);
                lk.diffVal = String.format(Locale.getDefault(), "%.1f", total - lk.normalCredit);
                lk.isEvaluationLayer = "SESSION".equals(selected.type);
                list.add(lk);
            }
            list.sort((a, b) -> a.name.compareToIgnoreCase(b.name));

        } else {
            for (AssignmentDetail detail : allDetails) {
                if (detail.getSemesterSession() == null) continue;
                if (!Objects.equals(selected.value, detail.getSemesterSession().getSemSessionID())) continue;

                if (detail.getCourse() != null && detail.getAssignment() != null && detail.getLecturer() != null && detail.getSection() != null) {
                    // Skip Part-time assignments from calculation
                    if ("Part-time".equalsIgnoreCase(detail.getAssignment().getType())) continue;

                    double credits = WorkloadCalculator.calculateCourseCredits(
                            detail.getCourse(),
                            detail.getAssignment().getLoadPercentage()
                    );
                    LecturerKredit lk = new LecturerKredit(
                            detail.getLecturer().getLecturerName(),
                            detail.getCourse().getCourseName(),
                            detail.getSection().getSectionNumber(),
                            String.format(Locale.getDefault(), "%.1f", credits),
                            "",
                            false
                    );
                    lk.lecturer = detail.getLecturer();
                    list.add(lk);
                }
            }
            list.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        }
        
        KreditAdapter adapter = new KreditAdapter(list, item -> {
            Intent intent = new Intent(this, LecturerCreditDetailsActivity.class);
            if (item.lecturer != null) {
                intent.putExtra("LECTURER_ID", item.lecturer.getLecturerID());
            }
            intent.putExtra("LECTURER_NAME", item.name);
            intent.putExtra("lecturer_data", item.lecturer); // Pass the full object
            startActivity(intent);
        });
        binding.rvKreditBreakdown.setAdapter(adapter);
    }

    static class LecturerKredit {
        String name, subject, section, kreditVal, diffVal;
        boolean isSummary, isEvaluationLayer;
        double normalCredit;
        Lecturer lecturer;
        LecturerKredit(String name, String subject, String section, String kreditVal, String diffVal, boolean isSummary) {
            this.name = name; this.subject = subject; this.section = section;
            this.kreditVal = kreditVal; this.diffVal = diffVal; 
            this.isSummary = isSummary;
        }
    }

    static class KreditAdapter extends RecyclerView.Adapter<KreditAdapter.ViewHolder> {
        private List<LecturerKredit> list;
        private OnItemClickListener listener;

        interface OnItemClickListener { void onItemClick(LecturerKredit item); }

        KreditAdapter(List<LecturerKredit> list, OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kredit_breakdown, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LecturerKredit item = list.get(position);
            
            if (item.isSummary) {
                holder.tvName.setText(item.name);
                holder.tvSubject.setVisibility(View.GONE);
                holder.tvSection.setVisibility(View.GONE);
                
                holder.tvJumlahLabel.setText("Total Credits :");
                holder.tvJumlah.setText(item.kreditVal);
                
                if (item.isEvaluationLayer) {
                    holder.layoutDiff.setVisibility(View.VISIBLE);
                    try {
                        double diff = Double.parseDouble(item.diffVal);
                        if (diff > 0.05) {
                            holder.tvDiffLabel.setText("Overload :");
                            holder.tvDiff.setText(String.format(Locale.getDefault(), "+%.1f", diff));
                            holder.tvDiff.setBackgroundResource(R.drawable.bg_oval_red);
                        } else if (diff < -0.05) {
                            holder.tvDiffLabel.setText("Under :");
                            holder.tvDiff.setText(String.format(Locale.getDefault(), "%.1f", diff));
                            holder.tvDiff.setBackgroundResource(R.drawable.bg_oval_yellow);
                        } else {
                            holder.tvDiffLabel.setText("Status :");
                            holder.tvDiff.setText("Good");
                            holder.tvDiff.setBackgroundResource(R.drawable.bg_oval_green);
                        }
                    } catch (NumberFormatException e) {
                        holder.tvDiffLabel.setText("Status :");
                        holder.tvDiff.setText(item.diffVal);
                        holder.tvDiff.setBackgroundResource(R.drawable.bg_oval_green);
                    }
                } else {
                    holder.layoutDiff.setVisibility(View.GONE);
                }
                
            } else {
                holder.tvName.setText("lect: " + item.name);
                holder.tvSubject.setVisibility(View.VISIBLE);
                holder.tvSubject.setText("subject: " + item.subject);
                holder.tvSection.setVisibility(View.VISIBLE);
                holder.tvSection.setText("sec: " + item.section);
                
                holder.tvJumlahLabel.setText("Credits :");
                holder.tvJumlah.setText(item.kreditVal);
                
                holder.layoutDiff.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvSubject, tvSection, tvJumlah, tvDiff, tvJumlahLabel, tvDiffLabel;
            View layoutDiff;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvLecturerName);
                tvSubject = v.findViewById(R.id.tvSubjectInfo);
                tvSection = v.findViewById(R.id.tvSectionInfo);
                tvJumlahLabel = v.findViewById(R.id.tvJumlahLabel);
                tvJumlah = v.findViewById(R.id.tvJumlahKredit);
                tvDiffLabel = v.findViewById(R.id.tvDiffLabel);
                tvDiff = v.findViewById(R.id.tvLebihKurangKredit);
                layoutDiff = v.findViewById(R.id.layoutDiff);
            }
        }
    }
}
