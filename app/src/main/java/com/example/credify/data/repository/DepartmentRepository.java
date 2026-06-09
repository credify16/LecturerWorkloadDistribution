package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.Department;
import com.example.credify.data.remote.SupabaseDepartment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class DepartmentRepository {
    public CompletableFuture<List<Department>> getDepartments() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseDepartment.INSTANCE.getAll(cont)
                );
                JSONArray array = new JSONArray(data);
                List<Department> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    list.add(new Department(
                            obj.has("DepartmentID") ? obj.optString("DepartmentID") : obj.optString("departmentid"),
                            obj.has("DepartmentName") ? obj.optString("DepartmentName") : obj.optString("departmentname")
                    ));
                }
                return list;
            } catch (Exception e) {
                Log.e("DepartmentRepository", "Error", e);
                return new ArrayList<>();
            }
        });
    }
}
