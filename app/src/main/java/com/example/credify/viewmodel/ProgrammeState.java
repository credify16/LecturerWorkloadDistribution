package com.example.credify.viewmodel;

import com.example.credify.data.model.Programme;
import java.util.List;

public abstract class ProgrammeState {
    public static class Idle extends ProgrammeState {}
    public static class Loading extends ProgrammeState {}
    public static class Success extends ProgrammeState {
        private final List<Programme> programmes;
        public Success(List<Programme> programmes) { this.programmes = programmes; }
        public List<Programme> getProgrammes() { return programmes; }
    }
    public static class Empty extends ProgrammeState {}
    public static class Error extends ProgrammeState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class ActionSuccess extends ProgrammeState {
        private final String message;
        public ActionSuccess(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
