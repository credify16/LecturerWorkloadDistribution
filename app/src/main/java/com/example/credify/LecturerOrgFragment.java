package com.example.credify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.credify.data.model.Department;
import com.example.credify.data.repository.DepartmentRepository;
import com.example.credify.databinding.FragmentLecturerOrgBinding;
import java.util.List;

public class LecturerOrgFragment extends Fragment {

    private FragmentLecturerOrgBinding binding;
    private String positionId, programmeId, departmentId;

    public static LecturerOrgFragment newInstance(String posId, String progId, String deptId) {
        LecturerOrgFragment fragment = new LecturerOrgFragment();
        Bundle args = new Bundle();
        args.putString("position_id", posId);
        args.putString("programme_id", progId);
        args.putString("department_id", deptId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            positionId = getArguments().getString("position_id");
            programmeId = getArguments().getString("programme_id");
            departmentId = getArguments().getString("department_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerOrgBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvPositionId.setText(positionId != null ? positionId : "N/A");
        binding.tvTitleId.setText(programmeId != null ? programmeId : "N/A");
        binding.tvDepartmentId.setText(departmentId != null ? departmentId : "N/A");

        if (departmentId != null) {
            new DepartmentRepository().getDepartments().thenAccept(departments -> {
                for (Department d : departments) {
                    if (departmentId.equals(d.getDepartmentID())) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (binding != null) {
                                    binding.tvDepartmentId.setText(d.getDepartmentName());
                                }
                            });
                        }
                        break;
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}