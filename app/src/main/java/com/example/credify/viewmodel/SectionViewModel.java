package com.example.credify.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.credify.data.model.Campus;
import com.example.credify.data.model.Programme;
import com.example.credify.data.model.Section;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.data.repository.CampusRepository;
import com.example.credify.data.repository.ProgrammeRepository;
import com.example.credify.data.repository.SectionRepository;
import com.example.credify.data.repository.SemesterSessionRepository;
import java.util.List;

public class SectionViewModel extends ViewModel {
    private final SectionRepository repository = new SectionRepository();
    private final CampusRepository campusRepository = new CampusRepository();
    private final ProgrammeRepository programmeRepository = new ProgrammeRepository();
    private final SemesterSessionRepository sessionRepository = new SemesterSessionRepository();

    private final MutableLiveData<SectionState> sectionState = new MutableLiveData<>(new SectionState.Idle());
    private final MutableLiveData<List<Campus>> campuses = new MutableLiveData<>();
    private final MutableLiveData<List<Programme>> programmes = new MutableLiveData<>();
    private final MutableLiveData<List<SemesterSession>> sessions = new MutableLiveData<>();

    public LiveData<SectionState> getSectionState() { return sectionState; }
    public LiveData<List<Campus>> getCampuses() { return campuses; }
    public LiveData<List<Programme>> getProgrammes() { return programmes; }
    public LiveData<List<SemesterSession>> getSessions() { return sessions; }

    public void fetchSections() {
        sectionState.setValue(new SectionState.Loading());
        repository.getSections().thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                sectionState.postValue(new SectionState.Empty());
            } else {
                list.sort((s1, s2) -> s1.getSectionNumber().compareToIgnoreCase(s2.getSectionNumber()));
                sectionState.postValue(new SectionState.Success(list));
            }
        }).exceptionally(ex -> {
            sectionState.postValue(new SectionState.Error(ex.getMessage()));
            return null;
        });
    }

    public void fetchSectionsByProgramme(String progId) {
        sectionState.setValue(new SectionState.Loading());
        repository.getSections().thenAccept(allSections -> {
            if (allSections == null || allSections.isEmpty()) {
                sectionState.postValue(new SectionState.Empty());
            } else {
                List<Section> filtered = new java.util.ArrayList<>();
                for (Section s : allSections) {
                    if (progId.equals(s.getProgrammeID())) {
                        filtered.add(s);
                    }
                }
                if (filtered.isEmpty()) {
                    sectionState.postValue(new SectionState.Empty());
                } else {
                    filtered.sort((s1, s2) -> s1.getSectionNumber().compareToIgnoreCase(s2.getSectionNumber()));
                    sectionState.postValue(new SectionState.Success(filtered));
                }
            }
        }).exceptionally(ex -> {
            sectionState.postValue(new SectionState.Error(ex.getMessage()));
            return null;
        });
    }

    public void fetchDataForSpinners() {
        campusRepository.getCampuses().thenAccept(list -> {
            if (list != null) list.sort((c1, c2) -> c1.getCampusName().compareToIgnoreCase(c2.getCampusName()));
            campuses.postValue(list);
        });
        programmeRepository.getProgrammes().thenAccept(list -> {
            if (list != null) list.sort((p1, p2) -> p1.getProgrammeName().compareToIgnoreCase(p2.getProgrammeName()));
            programmes.postValue(list);
        });
        sessionRepository.getSemesterSessions().thenAccept(list -> {
            if (list != null) list.sort((s1, s2) -> s1.getSemSessionID().compareToIgnoreCase(s2.getSemSessionID()));
            sessions.postValue(list);
        });
    }

    public void addSection(Section section) {
        sectionState.setValue(new SectionState.Loading());
        repository.addSection(section).thenAccept(success -> {
            if (success) {
                sectionState.postValue(new SectionState.ActionSuccess("Section added successfully"));
            } else {
                sectionState.postValue(new SectionState.Error("Failed to add section"));
            }
        }).exceptionally(ex -> {
            sectionState.postValue(new SectionState.Error("Failed to add section"));
            return null;
        });
    }

    public void updateSection(Section section) {
        sectionState.setValue(new SectionState.Loading());
        repository.updateSection(section).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                sectionState.postValue(new SectionState.ActionSuccess("Section updated successfully"));
            } else {
                sectionState.postValue(new SectionState.Error(result));
            }
        });
    }

    public void deleteSection(String sectionId) {
        sectionState.setValue(new SectionState.Loading());
        repository.deleteSection(sectionId).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                sectionState.postValue(new SectionState.ActionSuccess("Section deleted successfully"));
            } else {
                sectionState.postValue(new SectionState.Error(result));
            }
        });
    }
}
