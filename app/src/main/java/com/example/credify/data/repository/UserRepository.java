package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.Admin;
import com.example.credify.data.model.Lecturer;
import com.example.credify.data.remote.SupabaseAuth;
import com.example.credify.data.remote.SupabaseLecturer;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserRepository {

    private String cleanErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null) return "An unexpected error occurred.";

        // If it's the specific Supabase "Error sending recovery email"
        if (message.contains("Error sending recovery email")) {
            return "Server error: The email service failed to send the recovery message. This could be due to invalid email settings in the dashboard.";
        }
        boolean cleaned = true;
        while (cleaned) {
            cleaned = false;
            if (message.startsWith("Error: ")) {
                message = message.substring(7);
                cleaned = true;
            } else if (message.startsWith("Exception: ")) {
                message = message.substring(11);
                cleaned = true;
            } else if (message.startsWith("io.github.jan.supabase.auth.exception.AuthRestException: ")) {
                message = message.substring(56);
                cleaned = true;
            }
        }

        // If it's a JSON string from Supabase
        if (message.contains("{")) {
            try {
                int start = message.indexOf("{");
                String jsonPart = message.substring(start);
                JSONObject obj = new JSONObject(jsonPart);
                if (obj.has("error_description")) {
                    return obj.getString("error_description");
                } else if (obj.has("error")) {
                    Object err = obj.get("error");
                    if (err instanceof String) return (String) err;
                } else if (obj.has("msg")) {
                    return obj.getString("msg");
                } else if (obj.has("message")) {
                    return obj.getString("message");
                }
            } catch (Exception jsonEx) {
                // Ignore parsing errors, fallback to raw message
            }
        }

        // Specific mappings for common Supabase errors
        if (message.contains("rate limit") || message.contains("Too Many Requests") || message.contains("429")) {
            return "Too many requests. Please wait a few minutes before trying again.";
        }
        if (message.contains("Invalid login credentials") || message.contains("invalid_credentials") || message.contains("Email not found")) {
            return "Invalid email or password.";
        }
        if (message.contains("Email not confirmed")) {
            return "Please confirm your email address before logging in.";
        }
        if (message.contains("User already registered")) {
            return "An account with this email already exists.";
        }
        if (message.contains("Token has expired") || message.contains("invalid_grant")) {
            return "The code has expired or is invalid. Please request a new one.";
        }
        if (message.contains("Password should be at least")) {
            return "Password is too short. It must be at least 6 characters.";
        }
        if (message.contains("Request timeout") || message.contains("timeout")) {
            return "Connection timed out. Please check your internet and try again.";
        }

        return message;
    }

    public CompletableFuture<String> login(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAuth.INSTANCE.login(email, password, cont)
                );
                return result ? "SUCCESS" : "Login failed.";
            } catch (Exception e) {
                Log.e("UserRepository", "Login Error", e);
                return cleanErrorMessage(e);
            }
        });
    }

    public CompletableFuture<String> signUp(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAuth.INSTANCE.signUp(email, password, cont)
                );
                return result ? "SUCCESS" : "Sign up failed.";
            } catch (Exception e) {
                Log.e("UserRepository", "SignUp Error", e);
                return cleanErrorMessage(e);
            }
        });
    }

    public void logout() {
        try {
            BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (scope, cont) -> SupabaseAuth.INSTANCE.logout(cont)
            );
        } catch (Exception e) {
            Log.e("UserRepository", "Logout Error", e);
        }
    }

    public CompletableFuture<String> resetPassword(String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (email == null || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    return "Please enter a valid email address.";
                }

                // 1. Check if email exists via the RPC workaround (Security Definer function)
                boolean existsInDb = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseLecturer.INSTANCE.emailExists(email, cont)
                );

                if (!existsInDb) {
                    return "This email is not registered in our system. Please check the email or contact admin.";
                }

                // 2. Try to send reset email
                try {
                    boolean success = BuildersKt.runBlocking(
                            EmptyCoroutineContext.INSTANCE,
                            (scope, cont) -> SupabaseAuth.INSTANCE.resetPassword(email, cont)
                    );
                    return success ? "SUCCESS" : "Failed to send reset code. Please try again later.";
                } catch (Exception authEx) {
                    Log.e("UserRepository", "Auth Reset Error: " + authEx.toString(), authEx);
                    String rawMessage = authEx.getMessage() != null ? authEx.getMessage() : authEx.toString();
                    String cleaned = cleanErrorMessage(authEx);
                    
                    if (cleaned.contains("recovery email") || cleaned.contains("Too Many Requests") || cleaned.contains("limit")) {
                        if (rawMessage.contains("429") || rawMessage.toLowerCase().contains("rate")) {
                            return "Rate limit reached. Please wait and try again later.";
                        }
                    }
                    return "Email is registered, but the service failed: " + cleaned;
                }
            } catch (Exception e) {
                Log.e("UserRepository", "Reset Password General Error", e);
                return cleanErrorMessage(e);
            }
        });
    }

    public CompletableFuture<String> verifyOtp(String email, String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAuth.INSTANCE.verifyOtp(email, code, cont)
                );
                return "SUCCESS";
            } catch (Exception e) {
                Log.e("UserRepository", "Verify OTP Error", e);
                return cleanErrorMessage(e);
            }
        });
    }

    public CompletableFuture<String> updatePassword(String newPassword) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAuth.INSTANCE.updatePassword(newPassword, cont)
                );
                return "SUCCESS";
            } catch (Exception e) {
                Log.e("UserRepository", "Update Password Error", e);
                return cleanErrorMessage(e);
            }
        });
    }

    public boolean isUserLoggedIn() {
        try {
            return BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (scope, cont) -> SupabaseAuth.INSTANCE.isLoggedIn(cont)
            );
        } catch (Exception e) {
            return false;
        }
    }

    public CompletableFuture<Object> detectRole() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String userId = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAuth.INSTANCE.getUserId(cont)
                );

                Log.d("UserRepository", "Detecting role for userId: " + userId);
                if (userId == null) return null;

                // Check Admin table
                String adminJson = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseLecturer.INSTANCE.getAdminByUserId(userId, cont)
                );

                Log.d("UserRepository", "Admin raw response: " + adminJson);
                if (adminJson != null && !adminJson.equals("[]") && !adminJson.equals("null")) {
                    JSONArray array = new JSONArray(adminJson);
                    if (array.length() > 0) {
                        JSONObject obj = array.getJSONObject(0);
                        Admin admin = new Admin();
                        // Support both capitalized and lowercase keys from DB
                        admin.setAdminID(obj.has("AdminID") ? obj.optString("AdminID") : obj.optString("adminid"));
                        admin.setAdminName(obj.has("AdminName") ? obj.optString("AdminName") : obj.optString("adminname"));
                        admin.setEmail(obj.has("Email") ? obj.optString("Email") : obj.optString("email"));
                        admin.setAuth_user_id(obj.optString("auth_user_id"));
                        return admin;
                    }
                }

                // Check Lecturer table
                String lecturerJson = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseLecturer.INSTANCE.getByUserId(userId, cont)
                );

                Log.d("UserRepository", "Lecturer raw response: " + lecturerJson);
                if (lecturerJson != null && !lecturerJson.equals("[]") && !lecturerJson.equals("null")) {
                    JSONArray array = new JSONArray(lecturerJson);
                    if (array.length() > 0) {
                        JSONObject obj = array.getJSONObject(0);
                        Lecturer l = new Lecturer();
                        l.setLecturerID(obj.has("LecturerID") ? obj.optString("LecturerID") : obj.optString("lecturerid"));
                        l.setLecturerName(obj.has("LecturerName") ? obj.optString("LecturerName") : obj.optString("lecturername"));
                        l.setLecturerRole(obj.has("LecturerRole") ? obj.optString("LecturerRole") : obj.optString("lecturerrole"));
                        l.setPosition(obj.has("Position") ? obj.optString("Position") : obj.optString("position"));
                        l.setEmploymentType(obj.has("EmploymentType") ? obj.optString("EmploymentType") : obj.optString("employmenttype"));
                        l.setDepartmentID(obj.has("DepartmentID") ? obj.optString("DepartmentID") : obj.optString("departmentid"));
                        l.setProgrammeID(obj.has("ProgrammeID") ? obj.optString("ProgrammeID") : obj.optString("programmeid"));
                        l.setNormalBTSA(obj.isNull("NormalBTSA") ? 0.0 : obj.optDouble("NormalBTSA"));
                        l.setNormalCredit(obj.isNull("NormalCredit") ? 0.0 : obj.optDouble("NormalCredit"));
                        l.setAuth_user_id(obj.optString("auth_user_id"));
                        l.setEmail(obj.has("Email") ? obj.optString("Email") : obj.optString("email"));
                        return l;
                    }
                }

            } catch (Exception e) {
                Log.e("UserRepository", "Role Detection Error", e);
            }
            return null;
        });
    }
}
