package com.example.credify.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.credify.data.model.Lecturer;
import com.example.credify.databinding.ItemLecturerBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LecturerAdapter extends RecyclerView.Adapter<LecturerAdapter.ViewHolder> {

    private List<Lecturer> lecturers = new ArrayList<>();
    private List<Lecturer> lecturersFull = new ArrayList<>();
    private final Consumer<Lecturer> onItemClick;
    private final Consumer<Lecturer> onDeleteClick;

    public LecturerAdapter(List<Lecturer> lecturers,
                           Consumer<Lecturer> onItemClick,
                           Consumer<Lecturer> onDeleteClick) {

        if (lecturers != null) {
            this.lecturers = new ArrayList<>(lecturers);
            this.lecturersFull = new ArrayList<>(lecturers);
        }
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLecturerBinding binding = ItemLecturerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lecturer lecturer = lecturers.get(position);

        // ✅ Use YOUR actual getters
        holder.binding.tvLecturerName.setText(
                lecturer.getLecturerName() != null ? lecturer.getLecturerName() : "N/A"
        );

        holder.binding.tvLecturerRole.setText(
                (lecturer.getLecturerRole() != null ? lecturer.getLecturerRole() : "N/A") +
                (lecturer.getProgrammeID() != null && !lecturer.getProgrammeID().isEmpty() ? " | " + lecturer.getProgrammeID() : "")
        );

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.accept(lecturer);
            }
        });

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (onDeleteClick != null) {
                onDeleteClick.accept(lecturer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lecturers != null ? lecturers.size() : 0;
    }

    public void updateLecturers(List<Lecturer> newLecturers) {
        this.lecturers = newLecturers != null ? new ArrayList<>(newLecturers) : new ArrayList<>();
        this.lecturersFull = new ArrayList<>(this.lecturers);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        lecturers.clear();
        if (text.isEmpty()) {
            lecturers.addAll(lecturersFull);
        } else {
            String filterPattern = text.toLowerCase().trim();
            for (Lecturer item : lecturersFull) {
                if (item.getLecturerName().toLowerCase().contains(filterPattern) ||
                    item.getLecturerID().toLowerCase().contains(filterPattern) ||
                    (item.getLecturerRole() != null && item.getLecturerRole().toLowerCase().contains(filterPattern))) {
                    lecturers.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemLecturerBinding binding;

        ViewHolder(ItemLecturerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}