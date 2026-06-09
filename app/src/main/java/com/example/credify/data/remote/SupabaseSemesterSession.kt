package com.example.credify.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.util.Log

object SupabaseSemesterSession {
    private val client = SupabaseClientProvider.getClient()

    suspend fun getAll(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Semester_Session"].select()
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseSemSession", "Error", e)
            "[]"
        }
    }

    suspend fun insert(id: String, year: Int, semester: String, session: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = buildJsonObject {
                put("SemSessionID", id)
                put("Year", year)
                put("Semester", semester)
                put("Session", session)
            }
            client.postgrest["Semester_Session"].insert(data)
            true
        } catch (e: Exception) {
            Log.e("SupabaseSemSession", "Insert Error", e)
            false
        }
    }
    suspend fun update(
        semSessionID: String,
        year: Int,
        semester: String,
        session: String
    ): Boolean = withContext(Dispatchers.IO) {

        try {

            val data = buildJsonObject {
                put("Year", year)
                put("Semester", semester)
                put("Session", session)
            }

            client.postgrest["Semester_Session"]
                .update(
                    data
                ) {
                    filter {
                        eq("SemSessionID", semSessionID)
                    }
                }

            true

        } catch (e: Exception) {

            Log.e(
                "SupabaseSemSession",
                "Update Error",
                e
            )

            false
        }
    }
    suspend fun delete(
        semSessionID: String
    ): Boolean = withContext(Dispatchers.IO) {

        client.postgrest["Semester_Session"]
            .delete {
                filter {
                    eq("SemSessionID", semSessionID)
                }
            }

        true
    }
}
