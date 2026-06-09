package com.example.credify.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.credify.data.model.Programme;
import com.example.credify.databinding.ItemProgrammeBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProgrammeAdapter extends RecyclerView.Adapter<ProgrammeAdapter.ViewHolder> {

    private List<Programme> programmes = new ArrayList<>();
    private List<Programme> programmesFull = new ArrayList<>();
    private final Consumer<Programme> onItemClick;
    private final Consumer<Programme> onDeleteClick;

    public ProgrammeAdapter(List<Programme> programmes, Consumer<Programme> onItemClick, Consumer<Programme> onDeleteClick) {
        if (programmes != null) {
            this.programmes = programmes;
            this.programmesFull = new ArrayList<>(programmes);
        }
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProgrammeBinding binding = ItemProgrammeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Programme p = programmes.get(position);
        holder.binding.tvProgrammeId.setText(p.getProgrammeID());
        holder.binding.tvProgrammeName.setText(p.getProgrammeName());
        
        holder.itemView.setOnClickListener(v -> { if (onItemClick != null) onItemClick.accept(p); });
        holder.binding.btnDelete.setOnClickListener(v -> { if (onDeleteClick != null) onDeleteClick.accept(p); });
    }

    @Override
    public int getItemCount() { return programmes.size(); }

    public void updateProgrammes(List<Programme> newList) {
        this.programmes = newList != null ? newList : new ArrayList<>();
        this.programmesFull = new ArrayList<>(this.programmes);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String filterPattern = query.toLowerCase().trim();
        if (filterPattern.isEmpty()) {
            programmes = new ArrayList<>(programmesFull);
        } else {
            List<Programme> filteredList = new ArrayList<>();
            for (Programme item : programmesFull) {
                if (item.getProgrammeName().toLowerCase().contains(filterPattern) ||
                    item.getProgrammeID().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
            programmes = filteredList;
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemProgrammeBinding binding;
        ViewHolder(ItemProgrammeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
