package com.example.credify.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.credify.data.model.AssignmentDetail;
import com.example.credify.data.model.Course;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.data.repository.CourseRepository;
import com.example.credify.data.repository.SemesterSessionRepository;
import com.example.credify.data.repository.WorkloadRepository;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class WorkloadViewModel extends ViewModel {
    private final WorkloadRepository repository = new WorkloadRepository();
    private final SemesterSessionRepository sessionRepository = new SemesterSessionRepository();
    private final CourseRepository courseRepository = new CourseRepository();
    private final MutableLiveData<List<AssignmentDetail>> assignmentDetails = new MutableLiveData<>();
    private final MutableLiveData<List<SemesterSession>> semesterSessions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<AssignmentDetail>> getAssignmentDetails() { return assignmentDetails; }
    public LiveData<List<SemesterSession>> getSemesterSessions() { return semesterSessions; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void fetchAllDetails() {
        isLoading.setValue(true);
        
        // 1. Fetch Assignments with joined details
        repository.getAllAssignmentDetails().thenAccept(details -> {
            if (details == null) {
                isLoading.postValue(false);
                assignmentDetails.postValue(null);
                return;
            }

            // 2. Fetch all Courses to fill missing Course details (Supabase join safety)
            courseRepository.getCourses().thenAccept(courses -> {
                Map<String, Course> courseMap = new HashMap<>();
                for (Course c : courses) {
                    courseMap.put(c.getCourseCode(), c);
                }

                for (AssignmentDetail d : details) {
                    if (d.getCourse() == null && d.getAssignment() != null) {
                        d.setCourse(courseMap.get(d.getAssignment().getCourseCode()));
                    }
                }

                // Sorting
                details.sort((d1, d2) -> {
                    String n1 = d1.getLecturer() != null ? d1.getLecturer().getLecturerName() : "";
                    String n2 = d2.getLecturer() != null ? d2.getLecturer().getLecturerName() : "";
                    return n1.compareToIgnoreCase(n2);
                });

                isLoading.postValue(false);
                assignmentDetails.postValue(details);
            }).exceptionally(e -> {
                isLoading.postValue(false);
                errorMessage.postValue("Course Error: " + e.getMessage());
                return null;
            });

        }).exceptionally(e -> {
            isLoading.postValue(false);
            errorMessage.postValue(e.getMessage());
            return null;
        });
    }

    public void fetchSemesterSessions() {
        sessionRepository.getSemesterSessions().thenAccept(sessions -> {
            if (sessions != null) {
                sessions.sort((s1, s2) -> s1.getSemSessionID().compareToIgnoreCase(s2.getSemSessionID()));
            }
            semesterSessions.postValue(sessions);
        });
    }
}
