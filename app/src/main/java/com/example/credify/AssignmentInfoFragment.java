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
import com.example.credify.databinding.FragmentAssignmentInfoBinding;
import com.example.credify.viewmodel.AssignmentDetailViewModel;

public class AssignmentInfoFragment extends Fragment {

    private FragmentAssignmentInfoBinding binding;

    public static AssignmentInfoFragment newInstance() {
        return new AssignmentInfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAssignmentInfoBinding.inflate(inflater, container, false);
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
        
        binding.tvAssignmentId.setText(detail.getAssignment() != null ? detail.getAssignment().getAssignmentID() : "N/A");
        
        String courseText = "N/A";
        if (detail.getCourse() != null) {
            courseText = detail.getCourse().getCourseCode() + " - " + detail.getCourse().getCourseName();
        } else if (detail.getAssignment() != null) {
            courseText = detail.getAssignment().getCourseCode() + " (Details Pending)";
        }
        binding.tvCourseId.setText(courseText);

        binding.tvSectionId.setText(detail.getSection() != null ? detail.getSection().getSectionNumber() : "N/A");
        binding.tvSemester.setText(detail.getSemesterSession() != null ? detail.getSemesterSession().getSemester() : "N/A");
        binding.tvSession.setText(detail.getSemesterSession() != null ? detail.getSemesterSession().getSession() : "N/A");

        if (detail.getAssignment() != null) {
            String type = detail.getAssignment().getType();
            if (binding.tvType != null) {
                binding.tvType.setText(type != null ? type : "Full-time");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}