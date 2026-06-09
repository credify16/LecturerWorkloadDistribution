package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.Programme;
import com.example.credify.data.remote.SupabaseProgramme;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class ProgrammeRepository {
    public CompletableFuture<List<Programme>> getProgrammes() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseProgramme.INSTANCE.getAll(cont)
                );
                JSONArray array = new JSONArray(data);
                List<Programme> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    list.add(new Programme(
                            obj.optString("ProgrammeID"),
                            obj.optString("ProgrammeName")
                    ));
                }
                return list;
            } catch (Exception e) {
                Log.e("ProgrammeRepository", "Error", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Boolean> addProgramme(String id, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseProgramme.INSTANCE.insert(id, name, cont)
                );
            } catch (Exception e) {
                Log.e("ProgrammeRepository", "Add Error", e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> updateProgramme(String id, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseProgramme.INSTANCE.update(id, name, cont)
                );
            } catch (Exception e) {
                Log.e("ProgrammeRepository", "Update Error", e);
                return false;
            }
        });
    }

    public CompletableFuture<String> deleteProgramme(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseProgramme.INSTANCE.delete(id, cont)
                );
                return result ? "SUCCESS" : "Failed to delete programme.";
            } catch (Exception e) {
                Log.e("ProgrammeRepository", "Delete Error", e);
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                String lowerMsg = msg.toLowerCase();
                
                if (lowerMsg.contains("foreign key") || lowerMsg.contains("violates") || lowerMsg.contains("constraint") || lowerMsg.contains("23503") || lowerMsg.contains("database error")) {
                    return "This programme cannot be deleted because it is linked to lecturers, courses or sections.";
                }

                if (msg.contains("{")) {
                    try {
                        JSONObject obj = new JSONObject(msg.substring(msg.indexOf("{")));
                        if (obj.has("error")) {
                            String internal = obj.getString("error").toLowerCase();
                            if (internal.contains("foreign key") || internal.contains("violates") || internal.contains("23503") || internal.contains("database error")) {
                                return "This programme cannot be deleted because it is linked to lecturers, courses or sections.";
                            }
                            return obj.getString("error");
                        }
                    } catch (Exception ignored) {}
                }

                return "Failed to delete programme: " + msg;
            }
        });
    }
}
