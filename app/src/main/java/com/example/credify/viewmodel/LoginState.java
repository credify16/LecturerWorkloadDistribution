package com.example.credify.viewmodel;

import com.example.credify.data.model.Lecturer;

public abstract class LoginState {
    public static class Idle extends LoginState {}
    public static class Loading extends LoginState {}
    public static class SuccessAdmin extends LoginState {}
    public static class SignUpSuccess extends LoginState {}
    public static class ResetSuccess extends LoginState {}
    public static class OtpSent extends LoginState {}
    public static class PasswordUpdated extends LoginState {}
    
    public static class SuccessCoordinator extends LoginState {
        private final Lecturer lecturer;
        public SuccessCoordinator(Lecturer lecturer) { this.lecturer = lecturer; }
        public Lecturer getLecturer() { return lecturer; }
    }

    public static class SuccessLecturer extends LoginState {
        private final Lecturer lecturer;
        public SuccessLecturer(Lecturer lecturer) { this.lecturer = lecturer; }
        public Lecturer getLecturer() { return lecturer; }
    }
    
    public static class Error extends LoginState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
