package com.example.credify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.credify.data.model.AssignmentDetail;
import com.example.credify.databinding.FragmentLecturerInfoBinding;
import com.example.credify.viewmodel.AssignmentDetailViewModel;

public class LecturerInfoFragment extends Fragment {

    private FragmentLecturerInfoBinding binding;

    public static LecturerInfoFragment newInstance() {
        return new LecturerInfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        AssignmentDetailViewModel viewModel = new ViewModelProvider(requireActivity()).get(AssignmentDetailViewModel.class);
        viewModel.getAssignmentDetail().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(AssignmentDetail detail) {
        if (binding == null || detail == null) return;
        
        if (detail.getLecturer() != null) {
            binding.tvLecturerId.setText(detail.getLecturer().getLecturerName() + " (" + detail.getLecturer().getLecturerID() + ")");
        } else if (detail.getAssignment() != null) {
            binding.tvLecturerId.setText("ID: " + detail.getAssignment().getLecturerID());
        } else {
            binding.tvLecturerId.setText("N/A");
        }
        
        if (detail.getAssignment() != null && detail.getAssignment().getLoadPercentage() != null) {
            binding.tvLoadPercentage.setText(String.format("%.1f%%", detail.getAssignment().getLoadPercentage()));
        } else {
            binding.tvLoadPercentage.setText("0.0%");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}