package com.example.credify.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.credify.data.model.Programme;
import com.example.credify.data.repository.ProgrammeRepository;
import java.util.List;

public class ProgrammeViewModel extends ViewModel {
    private final ProgrammeRepository repository = new ProgrammeRepository();
    private final MutableLiveData<ProgrammeState> programmeState = new MutableLiveData<>(new ProgrammeState.Idle());

    public LiveData<ProgrammeState> getProgrammeState() { return programmeState; }

    public void fetchProgrammes() {
        programmeState.setValue(new ProgrammeState.Loading());
        repository.getProgrammes().thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                programmeState.postValue(new ProgrammeState.Empty());
            } else {
                list.sort((p1, p2) -> p1.getProgrammeName().compareToIgnoreCase(p2.getProgrammeName()));
                programmeState.postValue(new ProgrammeState.Success(list));
            }
        }).exceptionally(ex -> {
            programmeState.postValue(new ProgrammeState.Error(ex.getMessage()));
            return null;
        });
    }

    public void addProgramme(String id, String name) {
        programmeState.setValue(new ProgrammeState.Loading());
        repository.addProgramme(id, name).thenAccept(success -> {
            if (success) {
                programmeState.postValue(new ProgrammeState.ActionSuccess("Programme added successfully"));
            } else {
                programmeState.postValue(new ProgrammeState.Error("Failed to add programme"));
            }
        });
    }

    public void updateProgramme(String id, String name) {
        programmeState.setValue(new ProgrammeState.Loading());
        repository.updateProgramme(id, name).thenAccept(success -> {
            if (success) {
                programmeState.postValue(new ProgrammeState.ActionSuccess("Programme updated successfully"));
            } else {
                programmeState.postValue(new ProgrammeState.Error("Failed to update programme"));
            }
        });
    }

    public void deleteProgramme(String id) {
        programmeState.setValue(new ProgrammeState.Loading());
        repository.deleteProgramme(id).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                programmeState.postValue(new ProgrammeState.ActionSuccess("Programme deleted successfully"));
            } else {
                programmeState.postValue(new ProgrammeState.Error(result));
            }
        });
    }
}
