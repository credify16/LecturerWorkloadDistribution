package com.example.credify.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.credify.data.model.AssignmentDetail;
import com.example.credify.data.repository.AssignmentRepository;
import com.example.credify.data.repository.SectionRepository;
import com.example.credify.data.repository.SemesterSessionRepository;
import com.example.credify.data.repository.LecturerRepository;
import com.example.credify.data.repository.CourseRepository;

public class AssignmentDetailViewModel extends ViewModel {
    private final AssignmentRepository assignmentRepo = new AssignmentRepository();
    private final SectionRepository sectionRepo = new SectionRepository();
    private final SemesterSessionRepository sessionRepo = new SemesterSessionRepository();
    private final LecturerRepository lecturerRepo = new LecturerRepository();
    private final CourseRepository courseRepo = new CourseRepository();

    private final MutableLiveData<AssignmentDetail> assignmentDetail = new MutableLiveData<>();
    public LiveData<AssignmentDetail> getAssignmentDetail() { return assignmentDetail; }

    public void loadAssignment(String assignmentId) {
        if (assignmentId == null) return;
        
        assignmentRepo.getAssignmentById(assignmentId).thenAccept(assignment -> {
            if (assignment == null) return;
            
            AssignmentDetail detail = new AssignmentDetail();
            detail.setAssignment(assignment);
            
            // Start fetching related data
            fetchRelatedData(detail);
        });
    }

    private void fetchRelatedData(AssignmentDetail detail) {
        String courseCode = detail.getAssignment().getCourseCode();
        String sectionId = detail.getAssignment().getSectionID();
        String lecturerId = detail.getAssignment().getLecturerID();

        courseRepo.getCourseByCode(courseCode).thenAccept(course -> {
            detail.setCourse(course);
            notifyIfReady(detail);
        });

        sectionRepo.getSectionById(sectionId).thenAccept(section -> {
            detail.setSection(section);
            if (section != null) {
                sessionRepo.getSessionById(section.getSemSessionID()).thenAccept(session -> {
                    detail.setSemesterSession(session);
                    notifyIfReady(detail);
                });
            } else {
                notifyIfReady(detail);
            }
        });

        lecturerRepo.getLecturerById(lecturerId).thenAccept(lecturer -> {
            detail.setLecturer(lecturer);
            notifyIfReady(detail);
        });
    }

    private synchronized void notifyIfReady(AssignmentDetail detail) {
        // We notify on every partial update to ensure UI fills up as data arrives
        assignmentDetail.postValue(detail);
    }
}
