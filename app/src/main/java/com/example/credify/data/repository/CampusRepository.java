package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.Campus;
import com.example.credify.data.remote.SupabaseCampus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class CampusRepository {
    public CompletableFuture<List<Campus>> getCampuses() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseCampus.INSTANCE.getAll(cont)
                );
                JSONArray array = new JSONArray(data);
                List<Campus> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    list.add(new Campus(
                            obj.optString("CampusID"),
                            obj.optString("CampusName")
                    ));
                }
                return list;
            } catch (Exception e) {
                Log.e("CampusRepository", "Error", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<String> getCampusNameById(String id) {
        return getCampuses().thenApply(list -> {
            for (Campus c : list) {
                if (c.getCampusID().equals(id)) return c.getCampusName();
            }
            return id;
        });
    }
}
