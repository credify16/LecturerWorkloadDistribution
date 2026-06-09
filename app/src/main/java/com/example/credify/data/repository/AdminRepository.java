package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.Admin;
import com.example.credify.data.remote.SupabaseAdmin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class AdminRepository {

    public CompletableFuture<List<Admin>> getAdmins() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAdmin.INSTANCE.getAll(cont)
                );

                JSONArray array = new JSONArray(data);
                List<Admin> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Admin admin = new Admin();
                    admin.setAdminID(obj.has("AdminID") ? obj.optString("AdminID") : obj.optString("adminid"));
                    admin.setAdminName(obj.has("AdminName") ? obj.optString("AdminName") : obj.optString("adminname"));
                    admin.setEmail(obj.has("Email") ? obj.optString("Email") : obj.optString("email"));
                    if (admin.getEmail().isEmpty()) {
                        admin.setEmail(obj.optString("email"));
                    }
                    admin.setAuth_user_id(obj.optString("auth_user_id"));
                    list.add(admin);
                }
                return list;
            } catch (Exception e) {
                Log.e("AdminRepository", "Error getting admins", e);
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<String> addAdmin(String id, String name, String password, String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAdmin.INSTANCE.insert(id, name, password, email, cont)
                );
                return result ? "SUCCESS" : "Failed to add admin.";
            } catch (Exception e) {
                Log.e("AdminRepository", "Error adding admin: " + id, e);
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                if (msg.contains("already been registered") || msg.contains("email_change_confirm_already_exists")) {
                    return "This email address is already in use by another account.";
                }
                if (msg.contains("23505") || msg.contains("already exists") || msg.contains("duplicate")) {
                    return "An admin with this ID already exists.";
                }
                return "Failed to add admin.";
            }
        });
    }

    public CompletableFuture<String> updateAdmin(String id, String name, String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAdmin.INSTANCE.update(id, name, email, password, cont)
                );
                return result ? "SUCCESS" : "Failed to update admin.";
            } catch (Exception e) {
                Log.e("AdminRepository", "Error updating admin: " + id, e);
                return "Error: " + (e.getMessage() != null ? e.getMessage() : e.toString());
            }
        });
    }

    public CompletableFuture<String> deleteAdmin(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAdmin.INSTANCE.delete(id, cont)
                );
                return result ? "SUCCESS" : "Failed to delete admin.";
            } catch (Exception e) {
                Log.e("AdminRepository", "Error deleting admin: " + id, e);
                String error = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                String fullError = e.toString().toLowerCase();
                if (error.contains("foreign key") || error.contains("violates") || error.contains("constraint") || error.contains("23503") ||
                    fullError.contains("foreign key") || fullError.contains("violates") || fullError.contains("constraint") || fullError.contains("23503")) {
                    return "This admin cannot be deleted because it is linked to other records.";
                }
                return "Failed to delete admin: " + (e.getMessage() != null ? e.getMessage() : e.toString());
            }
        });
    }
}
