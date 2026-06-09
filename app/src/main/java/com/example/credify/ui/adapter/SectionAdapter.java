package com.example.credify.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.credify.data.model.Section;
import com.example.credify.databinding.ItemSectionBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {

    private List<Section> sections = new ArrayList<>();
    private List<Section> sectionsFull = new ArrayList<>();
    private final Consumer<Section> onItemClick;
    private final Consumer<Section> onDeleteClick;

    public SectionAdapter(List<Section> sections, Consumer<Section> onItemClick, Consumer<Section> onDeleteClick) {
        if (sections != null) {
            this.sections = sections;
            this.sectionsFull = new ArrayList<>(sections);
        }
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSectionBinding binding = ItemSectionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Section section = sections.get(position);
        
        // Use SectionNumber instead of SectionName
        holder.binding.tvSectionName.setText("Section " + (section.getSectionNumber() != null ? section.getSectionNumber() : "N/A"));
        
        // Show Programme or Campus info
        holder.binding.tvCourseID.setText(
            "Prog: " + section.getProgrammeID() + " | Students: " + section.getStudentAmount()
        );
        
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.accept(section);
        });
        
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (onDeleteClick != null) onDeleteClick.accept(section);
        });
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public void updateSections(List<Section> newSections) {
        this.sections = newSections != null ? newSections : new ArrayList<>();
        this.sectionsFull = new ArrayList<>(this.sections);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String filterPattern = query.toLowerCase().trim();
        if (filterPattern.isEmpty()) {
            sections = new ArrayList<>(sectionsFull);
        } else {
            List<Section> filteredList = new ArrayList<>();
            for (Section item : sectionsFull) {
                if ((item.getSectionNumber() != null && item.getSectionNumber().toLowerCase().contains(filterPattern)) ||
                    (item.getSectionID() != null && item.getSectionID().toLowerCase().contains(filterPattern)) ||
                    (item.getProgrammeID() != null && item.getProgrammeID().toLowerCase().contains(filterPattern))) {
                    filteredList.add(item);
                }
            }
            sections = filteredList;
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSectionBinding binding;
        ViewHolder(ItemSectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
