package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.Course;
import com.example.credify.data.remote.SupabaseCourse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class CourseRepository {

    public CompletableFuture<List<Course>> getCourses() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseCourse.INSTANCE.getAll(cont)
                );
                return parseCourses(data);
            } catch (Exception e) {
                Log.e("CourseRepository", "Error getting courses", e);
                return new ArrayList<>();
            }
        });
    }

    private List<Course> parseCourses(String data) throws Exception {
        JSONArray array = new JSONArray(data);
        List<Course> list = new ArrayList<>();
        for (int j = 0; j < array.length(); j++) {
            JSONObject obj = array.getJSONObject(j);
            Course c = new Course();
            c.setCourseCode(obj.has("CourseCode") ? obj.optString("CourseCode") : obj.optString("coursecode"));
            c.setCourseName(obj.has("CourseName") ? obj.optString("CourseName") : obj.optString("coursename"));
            c.setMethod(obj.has("Method") ? obj.optString("Method") : obj.optString("method"));
            c.setCreditValue(obj.isNull("CreditValue") ? (obj.isNull("creditvalue") ? 0.0 : obj.optDouble("creditvalue")) : obj.optDouble("CreditValue"));
            // Handle SSP -> WeeklyHour
            c.setWeeklyHour(obj.has("WeeklyHour") ? obj.optDouble("WeeklyHour") : (obj.has("SSP") ? obj.optDouble("SSP") : (obj.has("weeklyhour") ? obj.optDouble("weeklyhour") : obj.optDouble("ssp", 0.0))));
            c.setProgrammeID(obj.has("ProgrammeID") ? obj.optString("ProgrammeID") : obj.optString("programmeid"));
            list.add(c);
        }
        return list;
    }

    public CompletableFuture<Course> getCourseByCode(String code) {
        return getCourses().thenApply(list -> {
            for (Course c : list) {
                if (c.getCourseCode().equals(code)) return c;
            }
            return null;
        });
    }

    public CompletableFuture<String> addCourse(Course course) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseCourse.INSTANCE.insert(course, cont)
                );
                return result ? "SUCCESS" : "Failed to add course.";
            } catch (Exception e) {
                Log.e("CourseRepository", "Error adding course", e);
                return "Failed to add course.";
            }
        });
    }

    public CompletableFuture<String> updateCourse(Course course) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseCourse.INSTANCE.update(course, cont)
                );
                return result ? "SUCCESS" : "Failed to update course.";
            } catch (Exception e) {
                Log.e("CourseRepository", "Error updating course: " + course.getCourseCode(), e);
                return "Error: " + (e.getMessage() != null ? e.getMessage() : e.toString());
            }
        });
    }

    public CompletableFuture<String> deleteCourse(String courseCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseCourse.INSTANCE.delete(courseCode, cont)
                );
                return result ? "SUCCESS" : "Failed to delete course.";
            } catch (Exception e) {
                Log.e("CourseRepository", "Error deleting course: " + courseCode, e);
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                String lowerMsg = msg.toLowerCase();

                if (lowerMsg.contains("foreign key") || lowerMsg.contains("violates") || lowerMsg.contains("constraint") || lowerMsg.contains("23503") || lowerMsg.contains("database error")) {
                    return "This course cannot be deleted because it is linked to assignments or other records.";
                }

                if (msg.contains("{")) {
                    try {
                        JSONObject obj = new JSONObject(msg.substring(msg.indexOf("{")));
                        if (obj.has("error")) {
                            String internal = obj.getString("error").toLowerCase();
                            if (internal.contains("foreign key") || internal.contains("violates") || internal.contains("23503") || internal.contains("database error")) {
                                return "This course cannot be deleted because it is linked to assignments or other records.";
                            }
                            return obj.getString("error");
                        }
                    } catch (Exception ignored) {}
                }

                return "Failed to delete course: " + msg;
            }
        });
    }
}
