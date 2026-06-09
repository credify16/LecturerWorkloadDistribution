package com.example.credify.data.remote

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object SupabaseCampus {
    private val client = SupabaseClientProvider.getClient()

    suspend fun getAll(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["Campus"].select()
            response.data
        } catch (e: Exception) {
            Log.e("SupabaseCampus", "Error", e)
            "[]"
        }
    }
}
