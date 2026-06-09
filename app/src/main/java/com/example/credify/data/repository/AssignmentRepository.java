package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.Assignment;
import com.example.credify.data.remote.SupabaseAssignment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class AssignmentRepository {

    public CompletableFuture<List<Assignment>> getAssignments() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAssignment.INSTANCE.getAll(cont)
                );
                return parseAssignments(data);
            } catch (Exception e) {
                Log.e("AssignmentRepository", "Error getting assignments", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<List<Assignment>> getAssignmentsByLecturer(String lecturerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAssignment.INSTANCE.getByLecturer(lecturerId, cont)
                );
                return parseAssignments(data);
            } catch (Exception e) {
                Log.e("AssignmentRepository", "Error getting assignments for lecturer: " + lecturerId, e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<List<Assignment>> getAssignmentsBySection(String courseCode, String sectionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAssignment.INSTANCE.getBySection(courseCode, sectionId, cont)
                );
                return parseAssignments(data);
            } catch (Exception e) {
                Log.e("AssignmentRepository", "Error getting assignments for section", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<List<Assignment>> getAssignmentsByProgramme(String programmeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Get all courses to find which ones belong to the programme
                CourseRepository courseRepo = new CourseRepository();
                List<com.example.credify.data.model.Course> allCourses = courseRepo.getCourses().get();
                java.util.Set<String> programmeCourseCodes = new java.util.HashSet<>();
                for (com.example.credify.data.model.Course c : allCourses) {
                    if (programmeId.equals(c.getProgrammeID())) {
                        programmeCourseCodes.add(c.getCourseCode());
                    }
                }

                // 2. Get all assignments and filter
                List<Assignment> allAssignments = getAssignments().get();
                List<Assignment> filtered = new ArrayList<>();
                for (Assignment a : allAssignments) {
                    if (programmeCourseCodes.contains(a.getCourseCode())) {
                        filtered.add(a);
                    }
                }
                return filtered;
            } catch (Exception e) {
                Log.e("AssignmentRepository", "Error getting assignments for programme: " + programmeId, e);
                return new ArrayList<>();
            }
        });
    }

    private List<Assignment> parseAssignments(String data) throws org.json.JSONException {
        JSONArray array = new JSONArray(data);
        List<Assignment> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Assignment a = new Assignment();
            a.setAssignmentID(obj.has("AssignmentID") ? obj.optString("AssignmentID") : obj.optString("assignmentid"));
            a.setLecturerID(obj.has("LecturerID") ? obj.optString("LecturerID") : obj.optString("lecturerid"));
            a.setCourseCode(obj.has("CourseCode") ? obj.optString("CourseCode") : obj.optString("coursecode"));
            a.setSectionID(obj.has("SectionID") ? obj.optString("SectionID") : obj.optString("sectionid"));
            a.setLoadPercentage(obj.isNull("LoadPercentage") ? (obj.isNull("loadpercentage") ? 0.0 : obj.optDouble("loadpercentage")) : obj.optDouble("LoadPercentage"));
            a.setType(obj.has("Type") ? obj.optString("Type") : obj.optString("type"));
            list.add(a);
        }
        return list;
    }

    public CompletableFuture<Assignment> getAssignmentById(String id) {
        return getAssignments().thenApply(list -> {
            for (Assignment a : list) {
                if (a.getAssignmentID().equals(id)) return a;
            }
            return null;
        });
    }

    public CompletableFuture<String> addAssignment(Assignment assignment) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Fetch course to check teaching method
                CourseRepository courseRepo = new CourseRepository();
                com.example.credify.data.model.Course course = courseRepo.getCourseByCode(assignment.getCourseCode()).get();
                String method = (course != null && course.getMethod() != null) ? course.getMethod().toUpperCase() : "";

                // RULE 1 & 2: Validation
                String sectionData = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAssignment.INSTANCE.getBySection(assignment.getCourseCode(), assignment.getSectionID(), cont)
                );
                List<Assignment> existing = parseAssignments(sectionData);
                
                double currentLoad = 0;
                for (Assignment a : existing) {
                    if (a.getLecturerID().equals(assignment.getLecturerID())) {
                        return "This lecturer is already assigned to this course and section.";
                    }
                    currentLoad += a.getLoadPercentage();
                }

                // Allow exceeding 100% for M (LI), R (LI Report), and P (Project)
                boolean isSpecialMethod = "M".equals(method) || "R".equals(method) || "P".equals(method);
                if (!isSpecialMethod && (currentLoad + assignment.getLoadPercentage() > 100.0)) {
                    return "Total teaching load for this course section exceeds 100%. Capacity: " + (100.0 - currentLoad) + "%";
                }

                boolean success = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAssignment.INSTANCE.insert(
                                assignment.getAssignmentID(),
                                assignment.getLecturerID(),
                                assignment.getCourseCode(),
                                assignment.getSectionID(),
                                assignment.getLoadPercentage() != null ? assignment.getLoadPercentage() : 0.0,
                                assignment.getType(),
                                cont
                        )
                );
                return success ? "SUCCESS" : "Failed to add assignment.";
            } catch (Exception e) {
                Log.e("AssignmentRepository", "Error adding assignment", e);
                return "Failed to add assignment.";
            }
        });
    }

    public CompletableFuture<String> updateAssignment(Assignment assignment) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Fetch course to check teaching method
                CourseRepository courseRepo = new CourseRepository();
                com.example.credify.data.model.Course course = courseRepo.getCourseByCode(assignment.getCourseCode()).get();
                String method = (course != null && course.getMethod() != null) ? course.getMethod().toUpperCase() : "";

                // Rule 1 & 2 Validation
                String sectionData = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAssignment.INSTANCE.getBySection(assignment.getCourseCode(), assignment.getSectionID(), cont)
                );
                List<Assignment> existing = parseAssignments(sectionData);
                
                double currentLoad = 0;
                for (Assignment a : existing) {
                    if (a.getAssignmentID().equals(assignment.getAssignmentID())) continue;

                    if (a.getLecturerID().equals(assignment.getLecturerID())) {
                        return "This lecturer is already assigned to this course and section.";
                    }
                    currentLoad += a.getLoadPercentage();
                }

                // Allow exceeding 100% for M (LI), R (LI Report), and P (Project)
                boolean isSpecialMethod = "M".equals(method) || "R".equals(method) || "P".equals(method);
                if (!isSpecialMethod && (currentLoad + assignment.getLoadPercentage() > 100.0)) {
                    return "Total teaching load for this course section exceeds 100%. Capacity: " + (100.0 - currentLoad) + "%";
                }

                boolean success = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAssignment.INSTANCE.update(
                                assignment.getAssignmentID(),
                                assignment.getLecturerID(),
                                assignment.getCourseCode(),
                                assignment.getSectionID(),
                                assignment.getLoadPercentage() != null ? assignment.getLoadPercentage() : 0.0,
                                assignment.getType(),
                                cont
                        )
                );
                return success ? "SUCCESS" : "Failed to update assignment.";
            } catch (Exception e) {
                Log.e("AssignmentRepository", "Error updating assignment", e);
                return "Error: " + e.getMessage();
            }
        });
    }

    public CompletableFuture<String> deleteAssignment(String assignmentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAssignment.INSTANCE.delete(assignmentId, cont)
                );
                return result ? "SUCCESS" : "Failed to delete assignment.";
            } catch (Exception e) {
                Log.e("AssignmentRepository", "Error deleting assignment: " + assignmentId, e);
                String error = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                String fullError = e.toString().toLowerCase();
                if (error.contains("foreign key") || error.contains("violates") || error.contains("constraint") || error.contains("23503") ||
                    fullError.contains("foreign key") || fullError.contains("violates") || fullError.contains("constraint") || fullError.contains("23503")) {
                    return "This assignment cannot be deleted because it is linked to other records.";
                }
                return "Failed to delete assignment: " + (e.getMessage() != null ? e.getMessage() : e.toString());
            }
        });
    }
}
