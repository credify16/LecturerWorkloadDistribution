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
import com.example.credify.databinding.ActivityBtsaCalculationBinding;
import com.example.credify.utils.WorkloadCalculator;
import com.example.credify.viewmodel.WorkloadViewModel;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.credify.data.model.AssignmentDetail;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ActivityBtsaCalculationBinding;
import com.example.credify.utils.WorkloadCalculator;
import com.example.credify.viewmodel.WorkloadViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BtsaCalculationActivity extends AppCompatActivity {

    private ActivityBtsaCalculationBinding binding;
    private WorkloadViewModel viewModel;
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
        binding = ActivityBtsaCalculationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WorkloadViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayBtsaSummary();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.rvBtsaBreakdown.setLayoutManager(new LinearLayoutManager(this));
        
        observeViewModel();
        viewModel.fetchSemesterSessions();
        viewModel.fetchAllDetails();

        isReadOnly = getIntent().getBooleanExtra("is_read_only", false);
        if (isReadOnly) {
            binding.btnAddBtsa.setVisibility(View.INVISIBLE);
        }

        binding.btnAddBtsa.setOnClickListener(v -> {
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
            displayBtsaSummary();
        });
    }

    private void displayBtsaSummary() {
        SpinnerOption selected = (SpinnerOption) binding.spinnerSemester.getSelectedItem();
        if (selected == null) return;

        boolean isSummary = !"SEMESTER".equals(selected.type);
        if (isSummary) {
            binding.tvHeaderDetails.setText("Lecturer Name");
            binding.tvHeaderMetrics.setText("Workload Status");
        } else {
            binding.tvHeaderDetails.setText("Details (lect/subject/sec)");
            binding.tvHeaderMetrics.setText("Semester Hours");
        }

        String filterLid = getIntent().getStringExtra("LECTURER_ID");
        List<LecturerBtsa> list = new ArrayList<>();

        if (isSummary) {
            Map<String, LecturerBtsa> map = new HashMap<>();
            for (AssignmentDetail detail : allDetails) {
                if (detail.getSemesterSession() == null) continue;

                // Filtering Logic
                if ("SESSION".equals(selected.type)) {
                    if (!selected.value.equals(detail.getSemesterSession().getSession())) {
                        continue;
                    }
                }

                Lecturer l = detail.getLecturer();
                if (l == null) continue;
                String lid = l.getLecturerID();

                if (filterLid != null && !filterLid.isEmpty() && !lid.equals(filterLid)) {
                    continue;
                }

                LecturerBtsa lb = map.get(lid);
                if (lb == null) {
                    LecturerBtsa newLb = new LecturerBtsa(l.getLecturerName(), lid, 0.0, 0.0, true);
                    newLb.normalBtsa = WorkloadCalculator.getTargetBTSA(l);
                    newLb.lecturer = l;
                    map.put(lid, newLb);
                    lb = newLb;
                }

                if (detail.getCourse() != null && detail.getAssignment() != null) {
                    if ("Part-time".equalsIgnoreCase(detail.getAssignment().getType())) continue;

                    double btsa = WorkloadCalculator.calculateCourseBTSA(
                            detail.getCourse(),
                            detail.getSection(),
                            detail.getAssignment().getLoadPercentage()
                    );
                    lb.btsa += btsa;
                }
            }
            boolean isAnnual = "SESSION".equals(selected.type);
            for (LecturerBtsa lb : map.values()) {
                lb.lebih = lb.btsa - lb.normalBtsa;
                lb.isAnnual = isAnnual;
                list.add(lb);
            }
        } else {
            // Detailed View for Semester
            for (AssignmentDetail detail : allDetails) {
                if (detail.getSemesterSession() == null) continue;
                if (!Objects.equals(selected.value, detail.getSemesterSession().getSemSessionID())) continue;

                Lecturer l = detail.getLecturer();
                if (l == null) continue;
                String lid = l.getLecturerID();
                if (filterLid != null && !filterLid.isEmpty() && !lid.equals(filterLid)) {
                    continue;
                }

                if (detail.getCourse() != null && detail.getAssignment() != null && detail.getSection() != null) {
                    if ("Part-time".equalsIgnoreCase(detail.getAssignment().getType())) continue;

                    double btsa = WorkloadCalculator.calculateCourseBTSA(
                            detail.getCourse(),
                            detail.getSection(),
                            detail.getAssignment().getLoadPercentage()
                    );
                    LecturerBtsa lb = new LecturerBtsa(
                            l.getLecturerName(),
                            lid,
                            btsa,
                            0.0,
                            false,
                            detail.getCourse().getCourseName(),
                            detail.getSection().getSectionNumber()
                    );
                    lb.lecturer = l;
                    list.add(lb);
                }
            }
        }

        list.sort((a, b) -> a.name.compareToIgnoreCase(b.name));

        BtsaAdapter adapter = new BtsaAdapter(list, item -> {
            Intent intent = new Intent(this, LecturerCreditDetailsActivity.class);
            intent.putExtra("LECTURER_ID", item.id);
            intent.putExtra("LECTURER_NAME", item.name);
            intent.putExtra("lecturer_data", item.lecturer);
            intent.putExtra("is_read_only", isReadOnly);
            startActivity(intent);
        });
        binding.rvBtsaBreakdown.setAdapter(adapter);
    }

    static class LecturerBtsa {
        String name, id, subject, section;
        Lecturer lecturer;
        double btsa, lebih, normalBtsa;
        boolean isAnnual, isSummary;

        LecturerBtsa(String name, String id, double btsa, double lebih, boolean isSummary) {
            this.name = name;
            this.id = id;
            this.btsa = btsa;
            this.lebih = lebih;
            this.isSummary = isSummary;
        }

        LecturerBtsa(String name, String id, double btsa, double lebih, boolean isSummary, String subject, String section) {
            this(name, id, btsa, lebih, isSummary);
            this.subject = subject;
            this.section = section;
        }
    }

    static class BtsaAdapter extends RecyclerView.Adapter<BtsaAdapter.ViewHolder> {
        private List<LecturerBtsa> list;
        private OnItemClickListener listener;

        interface OnItemClickListener { void onItemClick(LecturerBtsa item); }

        BtsaAdapter(List<LecturerBtsa> list, OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_btsa_breakdown, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LecturerBtsa item = list.get(position);

            if (item.isSummary) {
                holder.tvName.setText(item.name);
                holder.tvSubject.setVisibility(View.GONE);
                holder.tvSection.setVisibility(View.GONE);

                holder.tvJumlahLabel.setText("Total Hours :");
                holder.tvBtsa.setText(String.format(Locale.getDefault(), "%.1f", item.btsa));

                if (item.isAnnual) {
                    holder.layoutDiff.setVisibility(View.VISIBLE);
                    if (item.lebih > 0.05) {
                        holder.tvDiffLabel.setText("Overload :");
                        holder.tvLebih.setText(String.format(Locale.getDefault(), "+%.1f", item.lebih));
                        holder.tvLebih.setBackgroundResource(R.drawable.bg_oval_red);
                    } else if (item.lebih < -0.05) {
                        holder.tvDiffLabel.setText("Under :");
                        holder.tvLebih.setText(String.format(Locale.getDefault(), "%.1f", item.lebih));
                        holder.tvLebih.setBackgroundResource(R.drawable.bg_oval_yellow);
                    } else {
                        holder.tvDiffLabel.setText("Status :");
                        holder.tvLebih.setText("Good");
                        holder.tvLebih.setBackgroundResource(R.drawable.bg_oval_green);
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

                holder.tvJumlahLabel.setText("Hours :");
                holder.tvBtsa.setText(String.format(Locale.getDefault(), "%.1f", item.btsa));

                holder.layoutDiff.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvSubject, tvSection, tvBtsa, tvLebih, tvJumlahLabel, tvDiffLabel;
            View layoutDiff;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvLecturerName);
                tvSubject = v.findViewById(R.id.tvSubject);
                tvSection = v.findViewById(R.id.tvSection);
                tvJumlahLabel = v.findViewById(R.id.tvJumlahLabel);
                tvBtsa = v.findViewById(R.id.tvBtsaHours);
                tvDiffLabel = v.findViewById(R.id.tvDiffLabel);
                tvLebih = v.findViewById(R.id.tvLebihJam);
                layoutDiff = v.findViewById(R.id.layoutDiff);
            }
        }
    }
}
