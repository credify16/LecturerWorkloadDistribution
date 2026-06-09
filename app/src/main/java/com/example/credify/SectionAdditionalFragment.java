package com.example.credify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.credify.databinding.FragmentSectionAdditionalBinding;

public class SectionAdditionalFragment extends Fragment {

    private FragmentSectionAdditionalBinding binding;
    private String programme;
    private String studentAmount;

    public static SectionAdditionalFragment newInstance(String programme, String studentAmount) {
        SectionAdditionalFragment fragment = new SectionAdditionalFragment();
        Bundle args = new Bundle();
        args.putString("programme", programme);
        args.putString("student_amount", studentAmount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            programme = getArguments().getString("programme");
            studentAmount = getArguments().getString("student_amount");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSectionAdditionalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvProgramme.setText(programme != null ? programme : "N/A");
        binding.tvStudentAmount.setText(String.valueOf(studentAmount));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
