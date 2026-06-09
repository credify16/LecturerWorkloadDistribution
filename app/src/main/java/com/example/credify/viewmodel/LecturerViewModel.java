package com.example.credify.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.credify.data.model.Department;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.Programme;
import com.example.credify.data.repository.DepartmentRepository;
import com.example.credify.data.repository.LecturerRepository;
import com.example.credify.data.repository.ProgrammeRepository;
import java.util.List;

public class LecturerViewModel extends ViewModel {
    private final LecturerRepository repository = new LecturerRepository();
    private final DepartmentRepository deptRepository = new DepartmentRepository();
    private final ProgrammeRepository progRepository = new ProgrammeRepository();

    private final MutableLiveData<LecturerState> lecturerState = new MutableLiveData<>(new LecturerState.Idle());
    private final MutableLiveData<List<Department>> departments = new MutableLiveData<>();
    private final MutableLiveData<List<com.example.credify.data.model.Programme>> programmes = new MutableLiveData<>();

    public LiveData<LecturerState> getLecturerState() {
        return lecturerState;
    }

    public LiveData<List<Department>> getDepartments() {
        return departments;
    }

    public LiveData<List<com.example.credify.data.model.Programme>> getProgrammes() {
        return programmes;
    }

    public void fetchLecturers() {
        lecturerState.setValue(new LecturerState.Loading());
        repository.getLecturers().thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                lecturerState.postValue(new LecturerState.Empty());
            } else {
                list.sort((l1, l2) -> l1.getLecturerName().compareToIgnoreCase(l2.getLecturerName()));
                lecturerState.postValue(new LecturerState.Success(list));
            }
        }).exceptionally(ex -> {
            Log.e("LecturerViewModel", "Fetch Error", ex);
            String errorMsg = ex.getMessage();
            if (errorMsg != null && errorMsg.contains("recursion")) {
                errorMsg = "Database Error: Infinite recursion in RLS policy. Please check Supabase policies for the Lecturer table.";
            }
            lecturerState.postValue(new LecturerState.Error(errorMsg != null ? errorMsg : "An unknown error occurred."));
            return null;
        });
    }

    public void fetchLecturersByProgramme(String progId) {
        lecturerState.setValue(new LecturerState.Loading());
        repository.getLecturersByProgramme(progId).thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                lecturerState.postValue(new LecturerState.Empty());
            } else {
                list.sort((l1, l2) -> l1.getLecturerName().compareToIgnoreCase(l2.getLecturerName()));
                lecturerState.postValue(new LecturerState.Success(list));
            }
        }).exceptionally(ex -> {
            lecturerState.postValue(new LecturerState.Error(ex.getMessage()));
            return null;
        });
    }

    public void fetchDepartments() {
        deptRepository.getDepartments().thenAccept(list -> {
            if (list != null) list.sort((d1, d2) -> d1.getDepartmentName().compareToIgnoreCase(d2.getDepartmentName()));
            departments.postValue(list);
        });
    }

    public void fetchProgrammes() {
        progRepository.getProgrammes().thenAccept(list -> {
            if (list != null) list.sort((p1, p2) -> p1.getProgrammeName().compareToIgnoreCase(p2.getProgrammeName()));
            programmes.postValue(list);
        });
    }

    public void addLecturer(Lecturer lecturer) {
        lecturerState.setValue(new LecturerState.Loading());
        repository.addLecturer(lecturer).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                lecturerState.postValue(new LecturerState.ActionSuccess("Lecturer added successfully"));
            } else {
                lecturerState.postValue(new LecturerState.Error(result));
            }
        }).exceptionally(ex -> {
            lecturerState.postValue(new LecturerState.Error(ex.getMessage()));
            return null;
        });
    }

    public void updateLecturer(Lecturer lecturer) {
        lecturerState.setValue(new LecturerState.Loading());
        repository.updateLecturer(lecturer).thenAccept(success -> {
            if (success) {
                lecturerState.postValue(new LecturerState.ActionSuccess("Lecturer updated successfully"));
            } else {
                lecturerState.postValue(new LecturerState.Error("Failed to update lecturer"));
            }
        }).exceptionally(ex -> {
            lecturerState.postValue(new LecturerState.Error(ex.getMessage()));
            return null;
        });
    }

    public void deleteLecturer(String lecturerId) {
        lecturerState.setValue(new LecturerState.Loading());
        repository.deleteLecturer(lecturerId).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                lecturerState.postValue(new LecturerState.ActionSuccess("Lecturer deleted successfully"));
            } else {
                lecturerState.postValue(new LecturerState.Error(result));
            }
        }).exceptionally(ex -> {
            lecturerState.postValue(new LecturerState.Error(ex.getMessage()));
            return null;
        });
    }
}
