package com.example.credify.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.credify.data.model.SemesterSession;
import com.example.credify.data.repository.SemesterSessionRepository;

public class SemesterSessionViewModel extends ViewModel {

    private final SemesterSessionRepository repository =
            new SemesterSessionRepository();

    private final MutableLiveData<SemesterSessionState>
            semesterSessionState =
            new MutableLiveData<>(new SemesterSessionState.Idle());

    public LiveData<SemesterSessionState>
    getSemesterSessionState() {

        return semesterSessionState;
    }

    public void fetchSemesterSessions() {

        semesterSessionState.setValue(
                new SemesterSessionState.Loading());

        repository.getSemesterSessions()
                .thenAccept(list -> {

                    if (list == null || list.isEmpty()) {

                        semesterSessionState.postValue(
                                new SemesterSessionState.Empty());

                    } else {
                        list.sort((s1, s2) -> s1.getSemSessionID().compareToIgnoreCase(s2.getSemSessionID()));
                        semesterSessionState.postValue(
                                new SemesterSessionState.Success(list));
                    }
                })
                .exceptionally(ex -> {

                    semesterSessionState.postValue(
                            new SemesterSessionState.Error(
                                    ex.getMessage()));

                    return null;
                });
    }

    public void addSemesterSession(
            SemesterSession session) {

        semesterSessionState.setValue(
                new SemesterSessionState.Loading());

        repository.addSemesterSession(session)
                .thenAccept(success -> {

                    if (success) {

                        semesterSessionState.postValue(
                                new SemesterSessionState.ActionSuccess(
                                        "Semester Session added successfully"
                                ));

                    } else {

                        semesterSessionState.postValue(
                                new SemesterSessionState.Error(
                                        "Failed to add Semester Session"
                                ));
                    }
                });
    }

    public void updateSemesterSession(
            SemesterSession session) {

        semesterSessionState.setValue(
                new SemesterSessionState.Loading());

        repository.updateSemesterSession(session)
                .thenAccept(success -> {

                    if (success) {

                        semesterSessionState.postValue(
                                new SemesterSessionState.ActionSuccess(
                                        "Semester Session updated successfully"
                                ));

                    } else {

                        semesterSessionState.postValue(
                                new SemesterSessionState.Error(
                                        "Failed to update Semester Session"
                                ));
                    }
                })
                .exceptionally(ex -> {

                    semesterSessionState.postValue(
                            new SemesterSessionState.Error(
                                    ex.getMessage()));

                    return null;
                });
    }

    public void deleteSemesterSession(String semSessionId) {
        semesterSessionState.setValue(new SemesterSessionState.Loading());
        repository.deleteSemesterSession(semSessionId).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                semesterSessionState.postValue(new SemesterSessionState.ActionSuccess("Semester Session deleted successfully"));
            } else {
                semesterSessionState.postValue(new SemesterSessionState.Error(result));
            }
        });
    }
}