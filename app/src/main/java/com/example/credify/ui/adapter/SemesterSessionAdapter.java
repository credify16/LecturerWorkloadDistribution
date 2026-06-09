package com.example.credify.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.credify.data.model.SemesterSession;
import com.example.credify.databinding.ItemSemesterSessionBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SemesterSessionAdapter extends RecyclerView.Adapter<SemesterSessionAdapter.ViewHolder> {

    private List<SemesterSession> semesterSessions =
            new ArrayList<>();
    private List<SemesterSession> semesterSessionsFull =
            new ArrayList<>();

    private final Consumer<SemesterSession> onItemClick;

    private final Consumer<SemesterSession> onDeleteClick;

    public SemesterSessionAdapter(
            List<SemesterSession> semesterSessions,
            Consumer<SemesterSession> onItemClick,
            Consumer<SemesterSession> onDeleteClick
    ) {

        if (semesterSessions != null) {
            this.semesterSessions = semesterSessions;
            this.semesterSessionsFull = new ArrayList<>(semesterSessions);
        }

        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        ItemSemesterSessionBinding binding =
                ItemSemesterSessionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        SemesterSession semester =
                semesterSessions.get(position);

        holder.binding.tvSemesterInfo.setText(
                semester.getSemester()
                        + " "
                        + semester.getYear()
        );

        holder.binding.tvDetails.setText(
                "Session: "
                        + semester.getSession()
        );

        holder.itemView.setOnClickListener(v -> {

            if (onItemClick != null) {
                onItemClick.accept(semester);
            }
        });

        holder.binding.btnDelete.setOnClickListener(v -> {

            if (onDeleteClick != null) {
                onDeleteClick.accept(semester);
            }
        });
    }

    @Override
    public int getItemCount() {
        return semesterSessions.size();
    }

    public void updateSemesterSessions(
            List<SemesterSession> newList
    ) {

        semesterSessions =
                newList != null
                        ? newList
                        : new ArrayList<>();
        semesterSessionsFull = new ArrayList<>(semesterSessions);

        notifyDataSetChanged();
    }

    public void filter(String query) {
        String filterPattern = query.toLowerCase().trim();
        if (filterPattern.isEmpty()) {
            semesterSessions = new ArrayList<>(semesterSessionsFull);
        } else {
            List<SemesterSession> filteredList = new ArrayList<>();
            for (SemesterSession item : semesterSessionsFull) {
                if (item.getSemester().toLowerCase().contains(filterPattern) ||
                    item.getSession().toLowerCase().contains(filterPattern) ||
                    String.valueOf(item.getYear()).contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
            semesterSessions = filteredList;
        }
        notifyDataSetChanged();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        final ItemSemesterSessionBinding binding;

        ViewHolder(ItemSemesterSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}