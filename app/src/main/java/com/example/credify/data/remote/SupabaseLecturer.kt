package com.example.credify.data.remote

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.util.Log
import io.ktor.client.statement.bodyAsText

object SupabaseLecturer {
    private val client = SupabaseClientProvider.getClient()

    suspend fun getAll(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Lecturer"].select()
            Log.d("SupabaseLecturer", "Fetch Success: ${response.data}")
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseLecturer", "Fetch Error", e)
            throw e
        }
    }

    suspend fun getByProgramme(progId: String): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Lecturer"].select {
                filter {
                    eq("ProgrammeID", progId)
                }
            }
            Log.d("SupabaseLecturer", "Fetch by Programme Success: ${response.data}")
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseLecturer", "Fetch by Programme Error", e)
            throw e
        }
    }

    suspend fun insert(id: String, name: String, passwordArg: String, position: String, role: String, btsa: Double, credit: Double, type: String, deptId: String, email: String, progId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("lecturerId", id)
                put("lecturerName", name)
                put("email", email)
                put("password", passwordArg)
                put("position", position)
                put("role", role)
                put("btsa", btsa)
                put("credit", credit)
                put("type", type)
                put("deptId", deptId)
                put("progId", progId)
            }

            val session = client.auth.currentSessionOrNull()

            if (session == null) {
                Log.e("SupabaseLecturer", "No session found. User is not logged in.")
                return@withContext false
            }

            // Using Edge Function to create Auth user and DB profile simultaneously
            val response = client.functions.invoke("create-lecturer",
                body = payload
            )
            
            if (response.status.value >= 400) {
                val errorMsg = response.bodyAsText()
                Log.e("SupabaseLecturer", "Insert Error: $errorMsg")
                throw Exception(errorMsg)
            }

            Log.d("SupabaseLecturer", "Insert Success via Function: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseLecturer", "Insert Error for $id", e)
            throw e
        }
    }

    suspend fun update(id: String, name: String, position: String, role: String, btsa: Double, credit: Double, type: String, deptId: String, email: String, progId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("lecturerId", id)
                put("lecturerName", name)
                put("position", position)
                put("role", role)
                put("btsa", btsa)
                put("credit", credit)
                put("type", type)
                put("deptId", deptId)
                put("email", email)
                put("progId", progId)
            }

            val session = client.auth.currentSessionOrNull()
            if (session == null) {
                Log.e("SupabaseLecturer", "No active session")
                return@withContext false
            }

            val response = client.functions.invoke("update-lecturer", body = payload)

            if (response.status.value >= 400) {
                val errorMsg = response.bodyAsText()
                Log.e("SupabaseLecturer", "Update Error: $errorMsg")
                throw Exception(errorMsg)
            }

            Log.d("SupabaseLecturer", "Update Success: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseLecturer", "Update Error for $id", e)
            throw e
        }
    }

    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("lecturerId", id)
            }

            val session = client.auth.currentSessionOrNull()
            if (session == null) {
                Log.e("SupabaseLecturer", "No active session")
                return@withContext false
            }

            // Using Edge Function to delete both Auth User and DB record
            val response = client.functions.invoke("delete-lecturer", body = payload)

            if (response.status.value >= 400) {
                val errorMsg = response.bodyAsText()
                Log.e("SupabaseLecturer", "Delete Error: $errorMsg")
                throw Exception(errorMsg)
            }

            Log.d("SupabaseLecturer", "Delete Success: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseLecturer", "Delete Exception for $id", e)
            throw e
        }
    }

    suspend fun getByUserId(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Lecturer"].select {
                filter {
                    eq("auth_user_id", userId)
                }
            }
            if (response.data == "[]") null else response.data
        } catch (e: Exception) {
            Log.e("SupabaseLecturer", "getByUserId Error", e)
            null
        }
    }
    
    suspend fun getAdminByUserId(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Admin"].select {
                filter {
                    eq("auth_user_id", userId)
                }
            }
            if (response.data == "[]") null else response.data
        } catch (e: Exception) {
            Log.e("SupabaseLecturer", "getAdminByUserId Error", e)
            null
        }
    }

    suspend fun emailExists(emailArg: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Call the PostgreSQL function via RPC
            val response = client.postgrest.rpc("check_email_exists", buildJsonObject {
                put("email_to_check", emailArg)
            })
            response.data.toBoolean()
        } catch (e: Exception) {
            Log.e("SupabaseLecturer", "RPC email check error", e)
            false
        }
    }
}
