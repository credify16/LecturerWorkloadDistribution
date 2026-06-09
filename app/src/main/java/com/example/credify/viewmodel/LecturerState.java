package com.example.credify.viewmodel;

import com.example.credify.data.model.Lecturer;
import java.util.List;

public abstract class LecturerState {
    public static class Idle extends LecturerState {}
    public static class Loading extends LecturerState {}
    public static class Success extends LecturerState {
        private final List<Lecturer> lecturers;
        public Success(List<Lecturer> lecturers) { this.lecturers = lecturers; }
        public List<Lecturer> getLecturers() { return lecturers; }
    }
    public static class Empty extends LecturerState {}
    public static class Error extends LecturerState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class ActionSuccess extends LecturerState {
        private final String message;
        public ActionSuccess(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
