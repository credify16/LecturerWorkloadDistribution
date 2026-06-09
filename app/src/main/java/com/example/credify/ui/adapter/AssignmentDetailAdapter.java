package com.example.credify.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.credify.data.model.AssignmentDetail;
import com.example.credify.databinding.ItemAssignmentBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AssignmentDetailAdapter extends RecyclerView.Adapter<AssignmentDetailAdapter.ViewHolder> {

    private List<AssignmentDetail> details;
    private final Consumer<AssignmentDetail> onItemClick;
    private final boolean isReadOnly;

    public AssignmentDetailAdapter(List<AssignmentDetail> details, Consumer<AssignmentDetail> onItemClick, boolean isReadOnly) {
        this.details = details != null ? details : new ArrayList<>();
        this.onItemClick = onItemClick;
        this.isReadOnly = isReadOnly;
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
        AssignmentDetail detail = details.get(position);

        if (detail.getAssignment() != null) {
            holder.binding.tvAssignmentInfo.setText(detail.getAssignment().getAssignmentID());
        }

        StringBuilder sb = new StringBuilder();
        if (detail.getCourse() != null) {
            sb.append(detail.getCourse().getCourseName()).append(" (").append(detail.getCourse().getCourseCode()).append(")");
        }
        if (detail.getSection() != null) {
            sb.append("\nSection: ").append(detail.getSection().getSectionNumber());
        }
        if (detail.getAssignment() != null && detail.getAssignment().getLoadPercentage() != null) {
            sb.append(" | Load: ").append(detail.getAssignment().getLoadPercentage()).append("%");
        }
        if (detail.getAssignment() != null && detail.getAssignment().getType() != null) {
            sb.append(" | ").append(detail.getAssignment().getType());
        }
        
        holder.binding.tvDetails.setText(sb.toString());

        if (isReadOnly) {
            holder.binding.btnDelete.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.accept(detail);
        });
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public void updateDetails(List<AssignmentDetail> newDetails) {
        this.details = newDetails != null ? newDetails : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAssignmentBinding binding;
        ViewHolder(ItemAssignmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
