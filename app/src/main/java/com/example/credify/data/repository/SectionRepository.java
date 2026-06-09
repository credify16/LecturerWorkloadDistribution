package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.Section;
import com.example.credify.data.remote.SupabaseSection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class SectionRepository {

    public CompletableFuture<List<Section>> getSections() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseSection.INSTANCE.getAll(cont)
                );
                
                JSONArray array = new JSONArray(data);
                List<Section> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Section s = new Section();
                    s.setSectionID(obj.has("SectionID") ? obj.optString("SectionID") : obj.optString("sectionid"));
                    s.setSectionNumber(obj.has("SectionNumber") ? obj.optString("SectionNumber") : obj.optString("sectionnumber"));
                    s.setCampusID(obj.has("CampusID") ? obj.optString("CampusID") : obj.optString("campusid"));
                    s.setStudentAmount(obj.has("StudentAmount") ? obj.optString("StudentAmount") : obj.optString("studentamount"));
                    s.setProgrammeID(obj.has("ProgrammeID") ? obj.optString("ProgrammeID") : obj.optString("programmeid"));
                    s.setSemSessionID(obj.has("SemSessionID") ? obj.optString("SemSessionID") : obj.optString("semsessionid"));
                    list.add(s);
                }
                return list;
            } catch (Exception e) {
                Log.e("SectionRepository", "Error getting sections", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Section> getSectionById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseSection.INSTANCE.getAll(cont) // Filtering on client for now or add getById to SupabaseSection
                );
                JSONArray array = new JSONArray(data);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String sid = obj.has("SectionID") ? obj.optString("SectionID") : obj.optString("sectionid");
                    if (id.equals(sid)) {
                        Section s = new Section();
                        s.setSectionID(sid);
                        s.setSectionNumber(obj.has("SectionNumber") ? obj.optString("SectionNumber") : obj.optString("sectionnumber"));
                        s.setCampusID(obj.has("CampusID") ? obj.optString("CampusID") : obj.optString("campusid"));
                        s.setStudentAmount(obj.has("StudentAmount") ? obj.optString("StudentAmount") : obj.optString("studentamount"));
                        s.setProgrammeID(obj.has("ProgrammeID") ? obj.optString("ProgrammeID") : obj.optString("programmeid"));
                        s.setSemSessionID(obj.has("SemSessionID") ? obj.optString("SemSessionID") : obj.optString("semsessionid"));
                        return s;
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        });
    }

    public CompletableFuture<Boolean> addSection(Section section) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseSection.INSTANCE.insert(
                                section.getSectionID(),
                                section.getSectionNumber(),
                                section.getCampusID(),
                                section.getStudentAmount() != null ? section.getStudentAmount() : "0",
                                section.getProgrammeID(),
                                section.getSemSessionID(),
                                cont
                        )
                );
            } catch (Exception e) {
                Log.e("SectionRepository", "Error adding section: " + section.getSectionID(), e);
                return false;
            }
        });
    }

    public CompletableFuture<String> updateSection(Section section) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseSection.INSTANCE.update(
                                section.getSectionID(),
                                section.getSectionNumber(),
                                section.getCampusID(),
                                section.getStudentAmount() != null ? section.getStudentAmount() : "0",
                                section.getProgrammeID(),
                                section.getSemSessionID(),
                                cont
                        )
                );
                return result ? "SUCCESS" : "Failed to update section.";
            } catch (Exception e) {
                Log.e("SectionRepository", "Error updating section: " + section.getSectionID(), e);
                return "Error: " + (e.getMessage() != null ? e.getMessage() : e.toString());
            }
        });
    }

    public CompletableFuture<String> deleteSection(String sectionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseSection.INSTANCE.delete(sectionId, cont)
                );
                return result ? "SUCCESS" : "Failed to delete section.";
            } catch (Exception e) {
                Log.e("SectionRepository", "Error deleting section: " + sectionId, e);
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                String lowerMsg = msg.toLowerCase();

                if (lowerMsg.contains("foreign key") || lowerMsg.contains("violates") || lowerMsg.contains("constraint") || lowerMsg.contains("23503") || lowerMsg.contains("database error")) {
                    return "This section cannot be deleted because it is linked to assignments or other records.";
                }

                if (msg.contains("{")) {
                    try {
                        JSONObject obj = new JSONObject(msg.substring(msg.indexOf("{")));
                        if (obj.has("error")) {
                            String internal = obj.getString("error").toLowerCase();
                            if (internal.contains("foreign key") || internal.contains("violates") || internal.contains("23503") || internal.contains("database error")) {
                                return "This section cannot be deleted because it is linked to assignments or other records.";
                            }
                            return obj.getString("error");
                        }
                    } catch (Exception ignored) {}
                }

                return "Failed to delete section: " + msg;
            }
        });
    }
}
