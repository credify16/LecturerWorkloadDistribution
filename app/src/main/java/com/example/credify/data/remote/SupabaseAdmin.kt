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

object SupabaseAdmin {
    private val client = SupabaseClientProvider.getClient()

    suspend fun getAll(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Admin"].select()
            Log.d("SupabaseAdmin", "Fetch Success: ${response.data}")
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseAdmin", "Fetch Error", e)
            "[]"
        }
    }

    suspend fun insert(id: String, name: String, passwordArg: String, email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("adminId", id)
                put("adminName", name)
                put("email", email)
                put("password", passwordArg)
            }

            val session = client.auth.currentSessionOrNull()

            Log.d("SupabaseAdmin", "session=${session != null}")

            if (session == null) {
                Log.e("SupabaseAdmin", "No session found. User is not logged in.")
                return@withContext false
            }

            // Using Supabase Edge Function to create admin and auth user simultaneously
            val response = client.functions.invoke("create-admin",
                body = payload
            )
            
            if (response.status.value >= 400) {
                val errorMsg = response.bodyAsText()
                Log.e("SupabaseAdmin", "Insert Error: $errorMsg")
                throw Exception(errorMsg)
            }

            Log.d("SupabaseAdmin", "Insert (via Function) Success: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseAdmin", "Insert Error for $id", e)
            throw e
        }
    }

    suspend fun update(id: String, name: String, email: String, passwordArg: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("adminId", id)
                put("adminName", name)
                put("email", email)
                if (!passwordArg.isNullOrEmpty()) {
                    put("password", passwordArg)
                }
            }

            val session = client.auth.currentSessionOrNull()
            if (session == null) {
                Log.e("SupabaseAdmin", "No active session")
                return@withContext false
            }

            val response = client.functions.invoke("update-admin", body = payload)

            if (response.status.value >= 400) {
                val errorMsg = response.bodyAsText()
                Log.e("SupabaseAdmin", "Update Error: $errorMsg")
                throw Exception(errorMsg)
            }

            Log.d("SupabaseAdmin", "Update Success: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseAdmin", "Update Error for $id", e)
            throw e
        }
    }

    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("adminId", id)
            }

            val session = client.auth.currentSessionOrNull()
            if (session == null) {
                Log.e("SupabaseAdmin", "No active session")
                return@withContext false
            }

            val response = client.functions.invoke("delete-admin", body = payload)
            
            if (response.status.value >= 400) {
                val errorMsg = response.bodyAsText()
                Log.e("SupabaseAdmin", "Delete Error: $errorMsg")
                throw Exception(errorMsg)
            }

            Log.d("SupabaseAdmin", "Delete Success: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseAdmin", "Delete Exception", e)
            throw e
        }
    }
}
