package com.example.credify.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.util.Log

object SupabaseSection {
    private val client = SupabaseClientProvider.getClient()

    suspend fun getAll(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Section"].select()
            Log.d("SupabaseSection", "Fetch Success: ${response.data}")
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseSection", "Fetch Error", e)
            "[]"
        }
    }

    suspend fun insert(id: String, number: String, campusId: String, studentAmount: String, programmeId: String, semSessionId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val section = buildJsonObject {
                put("SectionID", id)
                put("SectionNumber", number)
                put("CampusID", campusId)
                put("StudentAmount", studentAmount)
                put("ProgrammeID", programmeId)
                put("SemSessionID", semSessionId)
            }
            client.postgrest["Section"].insert(section)
            Log.d("SupabaseSection", "Insert Success: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseSection", "Insert Error for $id", e)
            false
        }
    }

    suspend fun update(id: String, number: String, campusId: String, studentAmount: String, programmeId: String, semSessionId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val section = buildJsonObject {
                put("SectionNumber", number)
                put("CampusID", campusId)
                put("StudentAmount", studentAmount)
                put("ProgrammeID", programmeId)
                put("SemSessionID", semSessionId)
            }
            client.postgrest["Section"].update(section) {
                filter {
                    eq("SectionID", id)
                }
            }
            Log.d("SupabaseSection", "Update Success: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseSection", "Update Error for $id", e)
            false
        }
    }

    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        client.postgrest["Section"].delete {
            filter {
                eq("SectionID", id)
            }
        }
        Log.d("SupabaseSection", "Delete Success: $id")
        true
    }
}
