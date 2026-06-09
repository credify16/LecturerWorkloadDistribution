package com.example.credify.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.util.Log

object SupabaseDepartment {
    private val client = SupabaseClientProvider.getClient()

    suspend fun getAll(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Department"].select()
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseDepartment", "Error", e)
            "[]"
        }
    }

    suspend fun insert(id: String, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = buildJsonObject {
                put("DepartmentID", id)
                put("DepartmentName", name)
            }
            client.postgrest["Department"].insert(data)
            true
        } catch (e: Exception) {
            Log.e("SupabaseDepartment", "Insert Error", e)
            false
        }
    }
}
