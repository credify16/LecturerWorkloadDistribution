package com.example.credify.viewmodel;

import com.example.credify.data.model.Course;
import java.util.List;

public abstract class CourseState {
    public static class Idle extends CourseState {}
    public static class Loading extends CourseState {}
    public static class Success extends CourseState {
        private final List<Course> courses;
        public Success(List<Course> courses) { this.courses = courses; }
        public List<Course> getCourses() { return courses; }
    }
    public static class Empty extends CourseState {}
    public static class Error extends CourseState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class ActionSuccess extends CourseState {
        private final String message;
        public ActionSuccess(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
