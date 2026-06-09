package com.example.credify.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.credify.data.model.Course;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.model.Programme;
import com.example.credify.data.repository.CourseRepository;
import com.example.credify.data.repository.LecturerRepository;
import com.example.credify.data.repository.ProgrammeRepository;
import java.util.List;

public class CourseViewModel extends ViewModel {

    private final CourseRepository repository = new CourseRepository();
    private final LecturerRepository lecturerRepository = new LecturerRepository();
    private final ProgrammeRepository programmeRepository = new ProgrammeRepository();

    private final MutableLiveData<CourseState> courseState = new MutableLiveData<>(new CourseState.Idle());
    private final MutableLiveData<List<Lecturer>> lecturers = new MutableLiveData<>();
    private final MutableLiveData<List<Programme>> programmes = new MutableLiveData<>();

    public LiveData<CourseState> getCourseState() { return courseState; }
    public LiveData<List<Lecturer>> getLecturers() { return lecturers; }
    public LiveData<List<Programme>> getProgrammes() { return programmes; }

    public void fetchCourses() {
        courseState.setValue(new CourseState.Loading());
        repository.getCourses().thenAccept(list -> {
            if (list == null || list.isEmpty()) {
                courseState.postValue(new CourseState.Empty());
            } else {
                list.sort((c1, c2) -> c1.getCourseName().compareToIgnoreCase(c2.getCourseName()));
                courseState.postValue(new CourseState.Success(list));
            }
        }).exceptionally(ex -> {
            courseState.postValue(new CourseState.Error(ex.getMessage()));
            return null;
        });
    }

    public void fetchCoursesByProgramme(String progId) {
        courseState.setValue(new CourseState.Loading());
        repository.getCourses().thenAccept(allCourses -> {
            if (allCourses == null || allCourses.isEmpty()) {
                courseState.postValue(new CourseState.Empty());
            } else {
                List<Course> filtered = new java.util.ArrayList<>();
                for (Course c : allCourses) {
                    if (progId.equals(c.getProgrammeID())) {
                        filtered.add(c);
                    }
                }
                if (filtered.isEmpty()) {
                    courseState.postValue(new CourseState.Empty());
                } else {
                    filtered.sort((c1, c2) -> c1.getCourseName().compareToIgnoreCase(c2.getCourseName()));
                    courseState.postValue(new CourseState.Success(filtered));
                }
            }
        }).exceptionally(ex -> {
            courseState.postValue(new CourseState.Error(ex.getMessage()));
            return null;
        });
    }

    public void fetchDataForSpinners() {
        lecturerRepository.getLecturers().thenAccept(list -> {
            if (list != null) list.sort((l1, l2) -> l1.getLecturerName().compareToIgnoreCase(l2.getLecturerName()));
            lecturers.postValue(list);
        });
        programmeRepository.getProgrammes().thenAccept(list -> {
            if (list != null) list.sort((p1, p2) -> p1.getProgrammeName().compareToIgnoreCase(p2.getProgrammeName()));
            programmes.postValue(list);
        });
    }

    public void fetchLecturersByProgramme(String progId) {
        lecturerRepository.getLecturersByProgramme(progId).thenAccept(list -> {
            if (list != null) list.sort((l1, l2) -> l1.getLecturerName().compareToIgnoreCase(l2.getLecturerName()));
            lecturers.postValue(list);
        });
    }

    public void addCourse(Course course) {
        courseState.setValue(new CourseState.Loading());
        repository.addCourse(course).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                courseState.postValue(new CourseState.ActionSuccess("Course added successfully"));
            } else {
                courseState.postValue(new CourseState.Error(result));
            }
        }).exceptionally(ex -> {
            courseState.postValue(new CourseState.Error(ex.getMessage()));
            return null;
        });
    }

    public void updateCourse(Course course) {
        courseState.setValue(new CourseState.Loading());
        repository.updateCourse(course).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                courseState.postValue(new CourseState.ActionSuccess("Course updated successfully"));
            } else {
                courseState.postValue(new CourseState.Error(result));
            }
        }).exceptionally(ex -> {
            courseState.postValue(new CourseState.Error(ex.getMessage()));
            return null;
        });
    }

    public void deleteCourse(String courseCode) {
        courseState.setValue(new CourseState.Loading());
        repository.deleteCourse(courseCode).thenAccept(result -> {
            if ("SUCCESS".equals(result)) {
                courseState.postValue(new CourseState.ActionSuccess("Course deleted successfully"));
            } else {
                courseState.postValue(new CourseState.Error(result));
            }
        }).exceptionally(ex -> {
            courseState.postValue(new CourseState.Error(ex.getMessage()));
            return null;
        });
    }
}
