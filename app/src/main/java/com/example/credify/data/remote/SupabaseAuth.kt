package com.example.credify.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object SupabaseAuth {
    private val client = SupabaseClientProvider.getClient()

    suspend fun login(emailArg: String, passwordArg: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.auth.signInWith(Email) {
                email = emailArg
                password = passwordArg
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Login failure for $emailArg", e)
            throw e // Throw to be caught by UserRepository
        }
    }

    suspend fun signUp(emailArg: String, passwordArg: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.auth.signUpWith(Email) {
                email = emailArg
                password = passwordArg
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "SignUp failure for $emailArg", e)
            false
        }
    }

    suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserId(): String? = withContext(Dispatchers.IO) {
        try {
            client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            client.auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun resetPassword(emailArg: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.auth.resetPasswordForEmail(emailArg)
            true
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Reset failure for $emailArg", e)
            throw e
        }
    }

    suspend fun verifyOtp(emailArg: String, code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.auth.verifyEmailOtp(type = OtpType.Email.RECOVERY, email = emailArg, token = code)
            true
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "OTP Verification failure for $emailArg", e)
            throw e
        }
    }

    suspend fun updatePassword(newPasswordArg: String): Boolean = withContext(Dispatchers.IO) {
        try {
            client.auth.updateUser {
                password = newPasswordArg
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Password update failure", e)
            throw e
        }
    }
}
