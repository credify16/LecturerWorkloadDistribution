package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.remote.SupabaseLecturer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class LecturerRepository {

    public CompletableFuture<List<Lecturer>> getLecturers() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseLecturer.INSTANCE.getAll(cont)
                );
                return parseLecturers(data);
            } catch (Exception e) {
                Log.e("LecturerRepository", "Error getting lecturers", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<List<Lecturer>> getLecturersByProgramme(String progId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseLecturer.INSTANCE.getByProgramme(progId, cont)
                );
                return parseLecturers(data);
            } catch (Exception e) {
                Log.e("LecturerRepository", "Error getting lecturers by programme: " + progId, e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Lecturer> getLecturerById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseLecturer.INSTANCE.getAll(cont)
                );
                JSONArray array = new JSONArray(data);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String lid = obj.has("LecturerID") ? obj.optString("LecturerID") : obj.optString("lecturerid");
                    if (id.equals(lid)) {
                        Lecturer l = new Lecturer();
                        l.setLecturerID(lid);
                        l.setLecturerName(obj.has("LecturerName") ? obj.optString("LecturerName") : obj.optString("lecturername"));
                        l.setPassword(obj.has("Password") ? obj.optString("Password") : obj.optString("password"));
                        l.setPosition(obj.has("Position") ? obj.optString("Position") : obj.optString("position"));
                        l.setLecturerRole(obj.has("LecturerRole") ? obj.optString("LecturerRole") : obj.optString("lecturerrole"));
                        l.setNormalBTSA(obj.isNull("NormalBTSA") ? (obj.isNull("normalbtsa") ? 0.0 : obj.optDouble("normalbtsa")) : obj.optDouble("NormalBTSA"));
                        l.setNormalCredit(obj.isNull("NormalCredit") ? (obj.isNull("normalcredit") ? 0.0 : obj.optDouble("normalcredit")) : obj.optDouble("NormalCredit"));
                        l.setEmploymentType(obj.has("EmploymentType") ? obj.optString("EmploymentType") : obj.optString("employmenttype"));
                        l.setDepartmentID(obj.has("DepartmentID") ? obj.optString("DepartmentID") : obj.optString("departmentid"));
                        l.setProgrammeID(obj.has("ProgrammeID") ? obj.optString("ProgrammeID") : obj.optString("programmeid"));
                        l.setEmail(obj.has("Email") ? obj.optString("Email") : obj.optString("email"));
                        l.setAuth_user_id(obj.has("Auth_user_id") ? obj.optString("Auth_user_id") : obj.optString("auth_user_id"));
                        return l;
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        });
    }

    private List<Lecturer> parseLecturers(String data) throws org.json.JSONException {
        Log.d("LecturerRepository", "Parsing lecturers data: " + data);
        JSONArray array = new JSONArray(data);
        List<Lecturer> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Lecturer l = new Lecturer();
            l.setLecturerID(obj.has("LecturerID") ? obj.optString("LecturerID") : obj.optString("lecturerid"));
            l.setLecturerName(obj.has("LecturerName") ? obj.optString("LecturerName") : obj.optString("lecturername"));
            l.setPassword(obj.has("Password") ? obj.optString("Password") : obj.optString("password"));
            l.setPosition(obj.has("Position") ? obj.optString("Position") : obj.optString("position"));
            l.setLecturerRole(obj.has("LecturerRole") ? obj.optString("LecturerRole") : obj.optString("lecturerrole"));
            l.setNormalBTSA(obj.isNull("NormalBTSA") ? (obj.isNull("normalbtsa") ? 0.0 : obj.optDouble("normalbtsa")) : obj.optDouble("NormalBTSA"));
            l.setNormalCredit(obj.isNull("NormalCredit") ? (obj.isNull("normalcredit") ? 0.0 : obj.optDouble("normalcredit")) : obj.optDouble("NormalCredit"));
            l.setEmploymentType(obj.has("EmploymentType") ? obj.optString("EmploymentType") : obj.optString("employmenttype"));
            l.setDepartmentID(obj.has("DepartmentID") ? obj.optString("DepartmentID") : obj.optString("departmentid"));
            l.setProgrammeID(obj.has("ProgrammeID") ? obj.optString("ProgrammeID") : obj.optString("programmeid"));
            l.setEmail(obj.has("Email") ? obj.optString("Email") : obj.optString("email"));
            l.setAuth_user_id(obj.has("Auth_user_id") ? obj.optString("Auth_user_id") : obj.optString("auth_user_id"));
            list.add(l);
        }
        return list;
    }
    public CompletableFuture<String> addLecturer(Lecturer lecturer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseLecturer.INSTANCE.insert(
                                lecturer.getLecturerID(),
                                lecturer.getLecturerName(),
                                lecturer.getPassword(),
                                lecturer.getPosition() != null ? lecturer.getPosition() : "",
                                lecturer.getLecturerRole(),
                                lecturer.getNormalBTSA() != null ? lecturer.getNormalBTSA() : 0.0,
                                lecturer.getNormalCredit() != null ? lecturer.getNormalCredit() : 0.0,
                                lecturer.getEmploymentType() != null ? lecturer.getEmploymentType() : "",
                                lecturer.getDepartmentID() != null ? lecturer.getDepartmentID() : "",
                                lecturer.getEmail() != null ? lecturer.getEmail() : "",
                                lecturer.getProgrammeID() != null ? lecturer.getProgrammeID() : "",
                                cont
                        )
                );
                return result ? "SUCCESS" : "Failed to add lecturer.";
            } catch (Exception e) {
                Log.e("LecturerRepository", "Error adding lecturer", e);
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                if (msg.contains("already been registered") || msg.contains("email_change_confirm_already_exists")) {
                    return "This email address is already in use by another account.";
                }
                if (msg.contains("23505") || msg.contains("already exists") || msg.contains("duplicate")) {
                    return "A lecturer with this ID already exists.";
                }
                return "Failed to add lecturer.";
            }
        });
    }

    public CompletableFuture<Boolean> updateLecturer(Lecturer lecturer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseLecturer.INSTANCE.update(
                                lecturer.getLecturerID(),
                                lecturer.getLecturerName(),
                                lecturer.getPosition() != null ? lecturer.getPosition() : "",
                                lecturer.getLecturerRole(),
                                lecturer.getNormalBTSA() != null ? lecturer.getNormalBTSA() : 0.0,
                                lecturer.getNormalCredit() != null ? lecturer.getNormalCredit() : 0.0,
                                lecturer.getEmploymentType() != null ? lecturer.getEmploymentType() : "",
                                lecturer.getDepartmentID() != null ? lecturer.getDepartmentID() : "",
                                lecturer.getEmail() != null ? lecturer.getEmail() : "",
                                lecturer.getProgrammeID() != null ? lecturer.getProgrammeID() : "",
                                cont
                        )
                );
            } catch (Exception e) {
                Log.e("LecturerRepository", "Error updating lecturer: " + lecturer.getLecturerID(), e);
                return false;
            }
        });
    }

    public CompletableFuture<String> deleteLecturer(String lecturerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseLecturer.INSTANCE.delete(lecturerId, cont)
                );
                return result ? "SUCCESS" : "Failed to delete lecturer.";
            } catch (Exception e) {
                Log.e("LecturerRepository", "Error deleting lecturer: " + lecturerId, e);
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                String lowerMsg = msg.toLowerCase();
                
                if (lowerMsg.contains("foreign key") || lowerMsg.contains("violates") || lowerMsg.contains("constraint") || lowerMsg.contains("23503") || lowerMsg.contains("database error")) {
                    return "This lecturer cannot be deleted because it is linked to assignments or other records.";
                }
                
                // If it's a JSON string from Supabase Edge Function
                if (msg.contains("{")) {
                    try {
                        int start = msg.indexOf("{");
                        JSONObject obj = new JSONObject(msg.substring(start));
                        if (obj.has("error")) {
                            String internal = obj.getString("error").toLowerCase();
                            if (internal.contains("foreign key") || internal.contains("violates") || internal.contains("23503") || internal.contains("database error")) {
                                return "This lecturer cannot be deleted because it is linked to assignments or other records.";
                            }
                            return obj.getString("error");
                        }
                    } catch (Exception ignored) {}
                }
                
                return "Failed to delete lecturer: " + msg;
            }
        });
    }
}
