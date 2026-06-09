package com.example.credify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.credify.databinding.FragmentCourseGeneralBinding;

public class CourseGeneralFragment extends Fragment {

    private FragmentCourseGeneralBinding binding;
    private String courseCode, courseName, method, programmeId;

    public static CourseGeneralFragment newInstance(String code, String name, String method, String progId) {
        CourseGeneralFragment fragment = new CourseGeneralFragment();
        Bundle args = new Bundle();
        args.putString("course_code", code);
        args.putString("course_name", name);
        args.putString("method", method);
        args.putString("prog_id", progId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseCode = getArguments().getString("course_code");
            courseName = getArguments().getString("course_name");
            method = getArguments().getString("method");
            programmeId = getArguments().getString("prog_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCourseGeneralBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvCourseCode.setText(courseCode != null ? courseCode : "N/A");
        binding.tvCourseName.setText(courseName != null ? courseName : "N/A");
        
        String methodFull = method;
        if (method != null) {
            switch (method.toUpperCase()) {
                case "B": methodFull = "Lecture + Lab/Practical (Biasa)"; break;
                case "K": methodFull = "Lecture Only (Kuliah/Khas)"; break;
                case "P": methodFull = "Project"; break;
                case "M": methodFull = "Industrial Training"; break;
                case "R": methodFull = "Report"; break;
            }
        }
        binding.tvMethod.setText(methodFull != null ? methodFull : "N/A");

        binding.tvDepartmentId.setText(programmeId != null ? "Prog: " + programmeId : "N/A");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}