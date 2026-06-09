package com.example.credify.data.remote

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.util.Log

object SupabaseAssignment {
    private val client = SupabaseClientProvider.getClient()

    suspend fun getAll(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Assignment"].select()
            Log.d("SupabaseAssignment", "Fetch Success: ${response.data}")
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseAssignment", "Fetch Error", e)
            "[]"
        }
    }

    suspend fun getByLecturer(lecturerId: String): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Assignment"].select {
                filter {
                    eq("LecturerID", lecturerId)
                }
            }
            Log.d("SupabaseAssignment", "Fetch by Lecturer Success: ${response.data}")
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseAssignment", "Fetch by Lecturer Error", e)
            "[]"
        }
    }

    suspend fun getAssignmentsWithDetails(): String = withContext(Dispatchers.IO) {
        try {
            // Fetch everything needed for calculations in one go
            // Using aliases or explicit FK column naming to ensure Course is joined correctly
            val response = client.postgrest["Assignment"].select(
                Columns.raw("*,Course:CourseCode(*),Section(*,Semester_Session(*)),Lecturer(*)")
            )
            Log.d("SupabaseAssignment", "Fetch Details Success: ${response.data}")
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseAssignment", "Fetch Details Error", e)
            "[]"
        }
    }

    suspend fun getBySection(courseCode: String, sectionId: String): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Assignment"].select {
                filter {
                    eq("CourseCode", courseCode)
                    eq("SectionID", sectionId)
                }
            }
            Log.d("SupabaseAssignment", "Fetch by Section Success: ${response.data}")
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseAssignment", "Fetch by Section Error", e)
            "[]"
        }
    }

    suspend fun insert(id: String, lecturerId: String, courseCode: String, sectionId: String, loadPercentage: Double, type: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val assignment = buildJsonObject {
                put("AssignmentID", id)
                put("LecturerID", lecturerId)
                put("CourseCode", courseCode)
                put("SectionID", sectionId)
                put("LoadPercentage", loadPercentage)
                put("Type", type)
            }
            client.postgrest["Assignment"].insert(assignment)
            Log.d("SupabaseAssignment", "Insert Success: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseAssignment", "Insert Error for $id", e)
            false
        }
    }

    suspend fun update(id: String, lecturerId: String, courseCode: String, sectionId: String, loadPercentage: Double, type: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val assignment = buildJsonObject {
                put("LecturerID", lecturerId)
                put("CourseCode", courseCode)
                put("SectionID", sectionId)
                put("LoadPercentage", loadPercentage)
                put("Type", type)
            }
            client.postgrest["Assignment"].update(assignment) {
                filter {
                    eq("AssignmentID", id)
                }
            }
            Log.d("SupabaseAssignment", "Update Success: $id")
            true
        } catch (e: Exception) {
            Log.e("SupabaseAssignment", "Update Error for $id", e)
            false
        }
    }

    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        client.postgrest["Assignment"].delete {
            filter {
                eq("AssignmentID", id)
            }
        }
        Log.d("SupabaseAssignment", "Delete Success: $id")
        true
    }
}
