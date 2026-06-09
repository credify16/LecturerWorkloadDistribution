package com.example.credify.viewmodel;

import com.example.credify.data.model.Admin;
import java.util.List;

public abstract class AdminState {
    public static class Idle extends AdminState {}
    public static class Loading extends AdminState {}
    public static class Success extends AdminState {
        private final List<Admin> admins;
        public Success(List<Admin> admins) { this.admins = admins; }
        public List<Admin> getAdmins() { return admins; }
    }
    public static class Empty extends AdminState {}
    public static class ActionSuccess extends AdminState {
        private final String message;
        public ActionSuccess(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
    public static class Error extends AdminState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
