package com.example.credify.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.credify.data.model.Assignment;
import com.example.credify.databinding.ItemAssignmentBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    private List<Assignment> assignments;
    private List<Assignment> assignmentsFull;
    private final Consumer<Assignment> onItemClick;
    private final Consumer<Assignment> onDeleteClick;

    // ✅ Single correct constructor
    public AssignmentAdapter(List<Assignment> assignments,
                             Consumer<Assignment> onItemClick,
                             Consumer<Assignment> onDeleteClick) {

        this.assignments = assignments != null ? assignments : new ArrayList<>();
        this.assignmentsFull = new ArrayList<>(this.assignments);
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAssignmentBinding binding = ItemAssignmentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Assignment assignment = assignments.get(position);

        // Bind data
        holder.binding.tvAssignmentInfo.setText(assignment.getAssignmentID());
        String typeStr = assignment.getType() != null ? " (" + assignment.getType() + ")" : "";
        holder.binding.tvDetails.setText(
                "Course: " + assignment.getCourseCode() + " | Section: " + assignment.getSectionID() + typeStr
        );

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.accept(assignment);
            }
        });

        // Delete click
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (onDeleteClick != null) {
                onDeleteClick.accept(assignment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assignments != null ? assignments.size() : 0;
    }

    // Update list
    public void updateAssignments(List<Assignment> newAssignments) {
        this.assignments = newAssignments != null ? newAssignments : new ArrayList<>();
        this.assignmentsFull = new ArrayList<>(this.assignments);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String filterPattern = query.toLowerCase().trim();
        if (filterPattern.isEmpty()) {
            assignments = new ArrayList<>(assignmentsFull);
        } else {
            List<Assignment> filteredList = new ArrayList<>();
            for (Assignment item : assignmentsFull) {
                if (item.getAssignmentID().toLowerCase().contains(filterPattern) ||
                    item.getCourseCode().toLowerCase().contains(filterPattern) ||
                    item.getLecturerID().toLowerCase().contains(filterPattern) ||
                    item.getSectionID().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
            assignments = filteredList;
        }
        notifyDataSetChanged();
    }

    // ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAssignmentBinding binding;

        ViewHolder(ItemAssignmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}