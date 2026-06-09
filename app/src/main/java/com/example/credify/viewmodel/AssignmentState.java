package com.example.credify.viewmodel;

import com.example.credify.data.model.Assignment;
import java.util.List;

public abstract class AssignmentState {

    public static class Idle extends AssignmentState {}

    public static class Loading extends AssignmentState {}

    // For LIST (fetching assignments)
    public static class Success extends AssignmentState {
        private final List<Assignment> assignments;

        public Success(List<Assignment> assignments) {
            this.assignments = assignments;
        }

        public List<Assignment> getAssignments() {
            return assignments;
        }
    }

    public static class Empty extends AssignmentState {}

    // ✅ NEW: for add/delete/update actions
    public static class ActionSuccess extends AssignmentState {
        private final String message;

        public ActionSuccess(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class Error extends AssignmentState {
        private final String message;

        public Error(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}