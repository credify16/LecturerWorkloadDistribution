package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.SemesterSession;
import com.example.credify.data.remote.SupabaseSemesterSession;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class SemesterSessionRepository {

    public CompletableFuture<List<SemesterSession>> getSemesterSessions() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseSemesterSession.INSTANCE.getAll(cont)
                );
                
                JSONArray array = new JSONArray(data);
                List<SemesterSession> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    list.add(new SemesterSession(
                            obj.has("SemSessionID") ? obj.optString("SemSessionID") : obj.optString("semsessionid"),
                            obj.has("Year") ? obj.optInt("Year") : obj.optInt("year"),
                            obj.has("Semester") ? obj.optString("Semester") : obj.optString("semester"),
                            obj.has("Session") ? obj.optString("Session") : obj.optString("session")
                    ));
                }
                return list;
            } catch (Exception e) {
                Log.e("SemSessionRepo", "Error", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<SemesterSession> getSessionById(String id) {
        return getSemesterSessions().thenApply(list -> {
            for (SemesterSession s : list) {
                if (s.getSemSessionID().equals(id)) return s;
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> addSemesterSession(SemesterSession session) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseSemesterSession.INSTANCE.insert(
                                session.getSemSessionID(),
                                session.getYear(),
                                session.getSemester(),
                                session.getSession(),
                                cont
                        )
                );
            } catch (Exception e) {
                Log.e("SemSessionRepo", "Error adding", e);
                return false;
            }
        });
    }
    public CompletableFuture<Boolean> updateSemesterSession(
            SemesterSession session) {

        return CompletableFuture.supplyAsync(() -> {

            try {

                return BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) ->
                                SupabaseSemesterSession.INSTANCE.update(
                                        session.getSemSessionID(),
                                        session.getYear(),
                                        session.getSemester(),
                                        session.getSession(),
                                        cont
                                )
                );

            } catch (Exception e) {

                Log.e(
                        "SemesterSessionRepository",
                        "Error updating semester",
                        e
                );

                return false;
            }
        });
    }
    public CompletableFuture<String> deleteSemesterSession(String semSessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseSemesterSession.INSTANCE.delete(semSessionId, cont)
                );
                return result ? "SUCCESS" : "Failed to delete semester session.";
            } catch (Exception e) {
                Log.e("SemesterSessionRepository", "Error deleting semester session", e);
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                String lowerMsg = msg.toLowerCase();

                if (lowerMsg.contains("foreign key") || lowerMsg.contains("violates") || lowerMsg.contains("constraint") || lowerMsg.contains("23503") || lowerMsg.contains("database error")) {
                    return "This semester session cannot be deleted because it is linked to sections or other records.";
                }

                if (msg.contains("{")) {
                    try {
                        JSONObject obj = new JSONObject(msg.substring(msg.indexOf("{")));
                        if (obj.has("error")) {
                            String internal = obj.getString("error").toLowerCase();
                            if (internal.contains("foreign key") || internal.contains("violates") || internal.contains("23503") || internal.contains("database error")) {
                                return "This semester session cannot be deleted because it is linked to sections or other records.";
                            }
                            return obj.getString("error");
                        }
                    } catch (Exception ignored) {}
                }

                return "Failed to delete semester session: " + msg;
            }
        });
    }
}
