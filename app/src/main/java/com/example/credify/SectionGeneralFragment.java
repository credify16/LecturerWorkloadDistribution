package com.example.credify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.credify.databinding.FragmentSectionGeneralBinding;

public class SectionGeneralFragment extends Fragment {

    private FragmentSectionGeneralBinding binding;
    private String sectionId;
    private String sectionNumber;
    private String semester;
    private String session;
    private String campus;

    public static SectionGeneralFragment newInstance(String sectionId, String sectionNumber, String semester, String session, String campus) {
        SectionGeneralFragment fragment = new SectionGeneralFragment();
        Bundle args = new Bundle();
        args.putString("section_id", sectionId);
        args.putString("section_number", sectionNumber);
        args.putString("semester", semester);
        args.putString("session", session);
        args.putString("campus", campus);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sectionId = getArguments().getString("section_id");
            sectionNumber = getArguments().getString("section_number");
            semester = getArguments().getString("semester");
            session = getArguments().getString("session");
            campus = getArguments().getString("campus");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSectionGeneralBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvSectionID.setText(sectionId != null ? sectionId : "N/A");
        binding.tvSectionNumber.setText(sectionNumber != null ? sectionNumber : "N/A");
        binding.tvSemester.setText(semester != null ? semester : "N/A");
        binding.tvSession.setText(session != null ? session : "N/A");
        binding.tvCampus.setText(campus != null ? campus : "N/A");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}