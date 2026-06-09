package com.example.credify.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.credify.data.model.Assignment;
import com.example.credify.data.model.Course;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.Section;
import com.example.credify.data.repository.AssignmentRepository;
import com.example.credify.data.repository.CourseRepository;
import com.example.credify.data.repository.LecturerRepository;
import com.example.credify.data.repository.SectionRepository;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.data.repository.SemesterSessionRepository;
import java.util.List;

public class AssignmentViewModel extends ViewModel {
    private final AssignmentRepository repository = new AssignmentRepository();
    private final LecturerRepository lecturerRepository = new LecturerRepository();
    private final CourseRepository courseRepository = new CourseRepository();
    private final SectionRepository sectionRepository = new SectionRepository();
    private final SemesterSessionRepository sessionRepository = new SemesterSessionRepository();

    private final MutableLiveData<AssignmentState> assignmentState = new MutableLiveData<>(new AssignmentState.Idle());
    private final MutableLiveData<List<Lecturer>> lecturers = new MutableLiveData<>();
    private final MutableLiveData<List<Course>> courses = new MutableLiveData<>();
    private final MutableLiveData<List<Section>> sections = new MutableLiveData<>();
    private final MutableLiveData<List<SemesterSession>> semesterSessions = new MutableLiveData<>();

    public LiveData<AssignmentState> getAssignmentState() { return assignmentState; }
    public LiveData<List<Lecturer>> getLecturers() { return lecturers; }
    public LiveData<List<Course>> getCourses() { return courses; }
    public LiveData<List<Section>> getSections() { return sections; }
    public LiveData<List<SemesterSession>> getSemesterSessions() { return semesterSessions; }

    private final MutableLiveData<List<Assignment>> sectionAssignments = new MutableLiveData<>();
    public LiveData<List<Assignment>> getSectionAssignments() { return sectionAssignments; }

    public void fetchSemesterSessions() {
        sessionRepository.getSemesterSessions().thenAccept(list -> {
            if (list != null) {
                list.sort((s1, s2) -> s1.getSemSessionID().compareToIgnoreCase(s2.getSemSessionID()));
            }
            semesterSessions.postValue(list);
        });
    }

    public void fetchAssignmentsBySection(String courseCode, String sectionId) {
        repository.getAssignmentsBySection(courseCode, sectionId).thenAccept(sectionAssignments::postValue);
    }

    public void fetchAssignments() {
        assignmentState.setValue(new AssignmentState.Loading());
        repository.getAssignments().thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                assignmentState.postValue(new AssignmentState.Empty());
            } else {
                list.sort((a, b) -> a.getAssignmentID().compareToIgnoreCase(b.getAssignmentID()));
                assignmentState.postValue(new AssignmentState.Success(list));
            }
        }).exceptionally(ex -> {
            assignmentState.postValue(new AssignmentState.Error(ex.getMessage()));
            return null;
        });
    }

    public void fetchAssignmentsByLecturer(String lecturerId) {
        assignmentState.setValue(new AssignmentState.Loading());
        repository.getAssignmentsByLecturer(lecturerId).thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                assignmentState.postValue(new AssignmentState.Empty());
            } else {
                list.sort((a, b) -> a.getAssignmentID().compareToIgnoreCase(b.getAssignmentID()));
                assignmentState.postValue(new AssignmentState.Success(list));
            }
        }).exceptionally(ex -> {
            assignmentState.postValue(new AssignmentState.Error(ex.getMessage()));
            return null;
        });
    }

    public void fetchAssignmentsByProgramme(String programmeId) {
        assignmentState.setValue(new AssignmentState.Loading());
        repository.getAssignmentsByProgramme(programmeId).thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                assignmentState.postValue(new AssignmentState.Empty());
            } else {
                list.sort((a, b) -> a.getAssignmentID().compareToIgnoreCase(b.getAssignmentID()));
                assignmentState.postValue(new AssignmentState.Success(list));
            }
        }).exceptionally(ex -> {
            assignmentState.postValue(new AssignmentState.Error(ex.getMessage()));
            return null;
        });
    }

    public void fetchDataForSpinners() {
        lecturerRepository.getLecturers().thenAccept(list -> {
            if (list != null) {
                list.sort((l1, l2) -> l1.getLecturerName().compareToIgnoreCase(l2.getLecturerName()));
            }
            lecturers.postValue(list);
        });
        courseRepository.getCourses().thenAccept(list -> {
            if (list != null) {
                list.sort((c1, c2) -> c1.getCourseName().compareToIgnoreCase(c2.getCourseName()));
            }
            courses.postValue(list);
        });
        sectionRepository.getSections().thenAccept(list -> {
            if (list != null) {
                list.sort((s1, s2) -> s1.getSectionNumber().compareToIgnoreCase(s2.getSectionNumber()));
            }
            sections.postValue(list);
        });
    }

    public void fetchDataForSpinnersByProgramme(String progId) {
        lecturerRepository.getLecturersByProgramme(progId).thenAccept(list -> {
            if (list != null) {
                list.sort((l1, l2) -> l1.getLecturerName().compareToIgnoreCase(l2.getLecturerName()));
            }
            lecturers.postValue(list);
        });
        
        courseRepository.getCourses().thenAccept(allCourses -> {
            List<Course> filtered = new java.util.ArrayList<>();
            for (Course c : allCourses) {
                if (progId.equals(c.getProgrammeID())) filtered.add(c);
            }
            filtered.sort((c1, c2) -> c1.getCourseName().compareToIgnoreCase(c2.getCourseName()));
            courses.postValue(filtered);
        });

        sectionRepository.getSections().thenAccept(allSections -> {
            List<Section> filtered = new java.util.ArrayList<>();
            for (Section s : allSections) {
                if (progId.equals(s.getProgrammeID())) filtered.add(s);
            }
            filtered.sort((s1, s2) -> s1.getSectionNumber().compareToIgnoreCase(s2.getSectionNumber()));
            sections.postValue(filtered);
        });
    }

    public void addAssignment(Assignment assignment) {
        assignmentState.setValue(new AssignmentState.Loading());
        repository.addAssignment(assignment).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                assignmentState.postValue(new AssignmentState.ActionSuccess("Assignment added successfully"));
            } else {
                assignmentState.postValue(new AssignmentState.Error(result));
            }
        });
    }

    public void updateAssignment(Assignment assignment) {
        assignmentState.setValue(new AssignmentState.Loading());
        repository.updateAssignment(assignment).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                assignmentState.postValue(new AssignmentState.ActionSuccess("Assignment updated successfully"));
            } else {
                assignmentState.postValue(new AssignmentState.Error(result));
            }
        });
    }

    public void deleteAssignment(String assignmentId) {
        assignmentState.setValue(new AssignmentState.Loading());
        repository.deleteAssignment(assignmentId).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                assignmentState.postValue(new AssignmentState.ActionSuccess("Assignment deleted successfully"));
            } else {
                assignmentState.postValue(new AssignmentState.Error(result));
            }
        });
    }
}
