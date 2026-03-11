package com.example.annapurna.data.repository

import android.util.Log
import com.example.annapurna.data.model.User
import com.example.annapurna.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.gotrue.providers.builtin.Email

class AuthRepository {

    private val client = SupabaseClientProvider.client

    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        userType: String
    ): Result<Unit> {
        return try {
            Log.d("AuthRepository", "register: email: $email, name: $name")

            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = client.auth.currentSessionOrNull()?.user?.id
                ?: return Result.failure(Exception("User ID null after signup"))

            Log.d("AuthRepository", "register: userId: $userId")

            val user = User(
                userId = userId,
                name = name,
                email = email,
                phone = phone,
                userType = userType
            )

            client.from("users").insert(user)
            Log.d("AuthRepository", "register: user inserted")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("AuthRepository", "register: failed", e)
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            Log.d("AuthRepository", "login: email: $email")
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("AuthRepository", "login: success")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "login: failed", e)
            Result.failure(e)
        }
    }

    suspend fun logout() {
        Log.d("AuthRepository", "logout")
        client.auth.signOut()
    }

    suspend fun updateFcmToken(userId: String, token: String?): Result<Unit> {
        return try {
            client.from("users")
                .update({ set("fcm_token", token) }) {
                    filter { eq("user_id", userId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "updateFcmToken failed", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserId(): String? {
        return try {
            client.auth.loadFromStorage()
            val userId = client.auth.currentUserOrNull()?.id
            Log.d("AuthRepository", "getCurrentUserId (from session): $userId")
            userId
        } catch (e: Exception) {
            Log.e("AuthRepository", "getCurrentUserId: failed", e)
            null
        }
    }

    suspend fun updateUserType(userId: String, userType: String): Result<Unit> {
        return try {
            Log.d("AuthRepository", "updateUserType: $userType")
            client.from("users")
                .update({ set("user_type", userType) }) {
                    filter { eq("user_id", userId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "updateUserType: failed", e)
            Result.failure(e)
        }
    }

    suspend fun getUserData(userId: String): Result<User> {
        return try {
            Log.d("AuthRepository", "getUserData: userId: $userId")
            val user = client
                .from("users")
                .select { filter { eq("user_id", userId) } }
                .decodeSingle<User>()
            Log.d("AuthRepository", "getUserData: success")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "getUserData: failed", e)
            Result.failure(e)
        }
    }
}
