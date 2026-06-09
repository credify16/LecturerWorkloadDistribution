package com.example.credify.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.credify.data.model.Course;
import com.example.credify.databinding.ItemCourseBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private List<Course> courses = new ArrayList<>();
    private List<Course> coursesFull = new ArrayList<>();
    private final Consumer<Course> onItemClick;
    private final Consumer<Course> onDeleteClick;

    public CourseAdapter(List<Course> courses,
                         Consumer<Course> onItemClick,
                         Consumer<Course> onDeleteClick) {
        if (courses != null) {
            this.courses = courses;
            this.coursesFull = new ArrayList<>(courses);
        }
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCourseBinding binding = ItemCourseBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);

        holder.binding.tvCourseCode.setText(
                course.getCourseCode() != null ? course.getCourseCode() : "N/A"
        );

        holder.binding.tvCourseName.setText(
                course.getCourseName() != null ? course.getCourseName() : "N/A"
        );

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.accept(course);
            }
        });

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (onDeleteClick != null) {
                onDeleteClick.accept(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses != null ? courses.size() : 0;
    }

    public void updateCourses(List<Course> newCourses) {
        this.courses = newCourses != null ? newCourses : new ArrayList<>();
        this.coursesFull = new ArrayList<>(this.courses);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        String filterPattern = query.toLowerCase().trim();
        if (filterPattern.isEmpty()) {
            courses = new ArrayList<>(coursesFull);
        } else {
            List<Course> filteredList = new ArrayList<>();
            for (Course item : coursesFull) {
                if (item.getCourseName().toLowerCase().contains(filterPattern) ||
                    item.getCourseCode().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
            courses = filteredList;
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemCourseBinding binding;

        ViewHolder(ItemCourseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
