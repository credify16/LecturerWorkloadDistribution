package com.example.credify.viewmodel;

import com.example.credify.data.model.SemesterSession;

import java.util.List;

public abstract class SemesterSessionState {

    public static class Idle
            extends SemesterSessionState {}

    public static class Loading
            extends SemesterSessionState {}

    public static class Success
            extends SemesterSessionState {

        private final List<SemesterSession>
                semesterSessions;

        public Success(
                List<SemesterSession> semesterSessions
        ) {
            this.semesterSessions = semesterSessions;
        }

        public List<SemesterSession>
        getSemesterSessions() {
            return semesterSessions;
        }
    }

    public static class Empty
            extends SemesterSessionState {}

    public static class Error
            extends SemesterSessionState {

        private final String message;

        public Error(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ActionSuccess
            extends SemesterSessionState {

        private final String message;

        public ActionSuccess(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}