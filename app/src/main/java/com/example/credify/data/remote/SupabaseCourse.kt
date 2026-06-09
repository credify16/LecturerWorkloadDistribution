package com.example.credify.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.util.Log
import com.example.credify.data.model.Course

object SupabaseCourse {
    private val client = SupabaseClientProvider.getClient()

    suspend fun getAll(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Course"].select()
            Log.d("SupabaseCourse", "Fetch Success: ${response.data}")
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseCourse", "Fetch Error", e)
            "[]"
        }
    }

    suspend fun insert(course: Course): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = buildJsonObject {
                put("CourseCode", course.getCourseCode())
                put("CourseName", course.getCourseName())
                put("Method", course.getMethod())
                put("CreditValue", course.getCreditValue())
                put("WeeklyHour", course.getWeeklyHour())
                put("ProgrammeID", course.getProgrammeID())
            }
            
            Log.d("SupabaseCourse", "Inserting Course: $json")
            
            client.postgrest["Course"].insert(json)
            true
        } catch (e: Exception) {
            Log.e("SupabaseCourse", "Insert Error for ${course.getCourseCode()}", e)
            throw e
        }
    }

    suspend fun update(course: Course): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = buildJsonObject {
                put("CourseName", course.getCourseName())
                put("Method", course.getMethod())
                put("CreditValue", course.getCreditValue())
                put("WeeklyHour", course.getWeeklyHour())
                put("ProgrammeID", course.getProgrammeID())
            }
            
            client.postgrest["Course"].update(json) {
                filter {
                    eq("CourseCode", course.getCourseCode())
                }
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseCourse", "Update Error for ${course.getCourseCode()}", e)
            throw e
        }
    }

    suspend fun delete(code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.postgrest["Course"].delete {
                filter {
                    eq("CourseCode", code)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseCourse", "Delete Error for $code", e)
            throw e
        }
    }
}
