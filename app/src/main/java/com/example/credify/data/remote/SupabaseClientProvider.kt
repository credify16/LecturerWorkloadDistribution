package com.example.credify.data.remote

import com.example.credify.SupabaseConfig
import io.github.jan.supabase.SupabaseClient

object SupabaseClientProvider {
    fun getClient(): SupabaseClient {
        return SupabaseConfig.getClient()
    }
}
