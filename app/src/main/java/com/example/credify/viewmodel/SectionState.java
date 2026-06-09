package com.example.credify.viewmodel;

import com.example.credify.data.model.Section;
import java.util.List;

public abstract class SectionState {
    public static class Idle extends SectionState {}
    public static class Loading extends SectionState {}
    public static class Success extends SectionState {
        private final List<Section> sections;
        public Success(List<Section> sections) { this.sections = sections; }
        public List<Section> getSections() { return sections; }
    }
    public static class Empty extends SectionState {}
    public static class Error extends SectionState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class ActionSuccess extends SectionState {
        private final String message;
        public ActionSuccess(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
