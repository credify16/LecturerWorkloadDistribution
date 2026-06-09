package com.example.credify.data.model;

public class AssignmentDetail {
    private Assignment assignment;
    private Course course;
    private Section section;
    private SemesterSession semesterSession;
    private Lecturer lecturer;

    public AssignmentDetail() {}

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public SemesterSession getSemesterSession() { return semesterSession; }
    public void setSemesterSession(SemesterSession semesterSession) { this.semesterSession = semesterSession; }

    public Lecturer getLecturer() { return lecturer; }
    public void setLecturer(Lecturer lecturer) { this.lecturer = lecturer; }
}
