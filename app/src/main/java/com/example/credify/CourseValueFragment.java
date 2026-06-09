package com.example.credify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.credify.databinding.FragmentCourseValueBinding;

public class CourseValueFragment extends Fragment {

    private FragmentCourseValueBinding binding;
    private double weeklyHours, creditValue;

    public static CourseValueFragment newInstance(double weeklyHours, double creditValue) {
        CourseValueFragment fragment = new CourseValueFragment();
        Bundle args = new Bundle();
        args.putDouble("weekly_hours", weeklyHours);
        args.putDouble("credit_value", creditValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            weeklyHours = getArguments().getDouble("weekly_hours");
            creditValue = getArguments().getDouble("credit_value");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCourseValueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvWeeklyHours.setText(String.valueOf(weeklyHours));
        binding.tvCreditValue.setText(String.valueOf(creditValue));
        // Load Value is now dynamically calculated by the formula engine, hiding it from static display if desired.
        // If the XML has tvLoadValue, we can set it to a dash or hide it.

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
