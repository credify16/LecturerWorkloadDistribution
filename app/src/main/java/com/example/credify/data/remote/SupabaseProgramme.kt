package com.example.credify.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object SupabaseProgramme {
    private val client = SupabaseClientProvider.getClient()

    suspend fun getAll(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Programme"].select()
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseProgramme", "Error", e)
            "[]"
        }
    }

    suspend fun insert(id: String, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = buildJsonObject {
                put("ProgrammeID", id)
                put("ProgrammeName", name)
            }
            client.postgrest["Programme"].insert(data)
            true
        } catch (e: Exception) {
            Log.e("SupabaseProgramme", "Insert Error", e)
            false
        }
    }

    suspend fun update(id: String, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = buildJsonObject {
                put("ProgrammeName", name)
            }
            client.postgrest["Programme"].update(data) {
                filter { eq("ProgrammeID", id) }
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseProgramme", "Update Error", e)
            false
        }
    }

    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest["Programme"].delete {
                filter { eq("ProgrammeID", id) }
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseProgramme", "Delete Error", e)
            false
        }
    }
}
