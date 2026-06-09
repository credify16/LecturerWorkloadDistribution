package com.example.credify;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.credify.data.model.Lecturer;
import com.example.credify.databinding.FragmentLecturerProfileBinding;

public class LecturerProfileFragment extends Fragment {

    private FragmentLecturerProfileBinding binding;
    private String lecturerId, lecturerName, employmentType;
    private Lecturer fullLecturer;
    private boolean isReadOnly = false;

    public static LecturerProfileFragment newInstance(Lecturer lecturer, boolean isReadOnly) {
        LecturerProfileFragment fragment = new LecturerProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("lecturer_data", lecturer);
        args.putBoolean("is_read_only", isReadOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fullLecturer = getArguments().getParcelable("lecturer_data");
            isReadOnly = getArguments().getBoolean("is_read_only", false);
            if (fullLecturer != null) {
                lecturerId = fullLecturer.getLecturerID();
                lecturerName = fullLecturer.getLecturerName();
                employmentType = fullLecturer.getEmploymentType();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvLecturerId.setText(lecturerId != null ? lecturerId : "N/A");
        binding.tvLecturerName.setText(lecturerName != null ? lecturerName : "N/A");
        binding.tvEmploymentType.setText(employmentType != null ? employmentType : "N/A");
        
        if (fullLecturer != null) {
            binding.tvEmail.setText(fullLecturer.getEmail() != null ? fullLecturer.getEmail() : "N/A");
            binding.tvRole.setText(fullLecturer.getLecturerRole() != null ? fullLecturer.getLecturerRole() : "N/A");
            binding.tvNormalBTSA.setText(fullLecturer.getNormalBTSA() != null ? String.format(java.util.Locale.getDefault(), "%.1f", fullLecturer.getNormalBTSA()) : "0.0");
            binding.tvNormalCredit.setText(fullLecturer.getNormalCredit() != null ? String.format(java.util.Locale.getDefault(), "%.1f", fullLecturer.getNormalCredit()) : "0.0");
        }

        binding.btnEditProfile.setVisibility(isReadOnly ? View.GONE : View.VISIBLE);
        binding.btnEditProfile.setOnClickListener(v -> {
            if (fullLecturer != null) {
                Intent intent = new Intent(getContext(), EditLecturerActivity.class);
                intent.putExtra("lecturer_data", fullLecturer);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}