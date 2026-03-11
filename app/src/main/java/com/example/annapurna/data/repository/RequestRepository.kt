package com.example.annapurna.data.repository

import android.content.Context
import android.util.Log
import com.example.annapurna.data.model.FoodRequest
import com.example.annapurna.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID


class RequestRepository(private val context: Context) {

    private val client = SupabaseClientProvider.client
    private val auth = client.auth

    companion object {
        private const val TAG = "RequestRepository"
    }

    suspend fun createRequest(
        foodType: String,  // ✅ Changed from foodName
        quantity: String,
        urgency: String,
        purpose: String,
        location: String,
        latitude: Double,
        longitude: Double,
        neededBy: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User not logged in"))

            // Get NGO name
            val userResult = client.from("users")
                .select {
                    filter { eq("user_id", user.id) }
                }
                .decodeSingleOrNull<com.example.annapurna.data.model.User>()

            val ngoName = userResult?.name ?: "Anonymous NGO"
            val requestId = UUID.randomUUID().toString()

            val request = FoodRequest(
                requestId = requestId,
                ngoId = user.id,
                ngoName = ngoName,
                foodType = foodType,  // ✅ Correct field
                quantity = quantity,
                urgency = urgency,
                purpose = purpose,
                location = location,
                latitude = latitude,
                longitude = longitude,
                neededBy = neededBy,
                status = "open",
                createdAt = System.currentTimeMillis()
            )

            client.from("food_requests").insert(request)

            Log.d(TAG, "Request created successfully: $requestId")
            Result.success(requestId)
        } catch (e: Exception) {
            Log.e(TAG, "createRequest failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchMyRequests(): Result<List<FoodRequest>> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User not logged in"))

            val list = client.from("food_requests")
                .select {
                    filter { eq("ngo_id", user.id) }  // ✅ Changed from user_id
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FoodRequest>()

            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "fetchMyRequests failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAllOpenRequests(): Result<List<FoodRequest>> = withContext(Dispatchers.IO) {
        try {
            val list = client.from("food_requests")
                .select {
                    filter { eq("status", "open") }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FoodRequest>()

            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllOpenRequests failed", e)
            Result.failure(e)
        }
    }

    suspend fun fulfillRequest(requestId: String, donorId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("food_requests")
                .update({
                    set("status", "fulfilled")
                }) {
                    filter { eq("request_id", requestId) }
                }

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "fulfillRequest failed", e)
            Result.failure(e)
        }
    }

    suspend fun cancelRequest(requestId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("food_requests")
                .update({
                    set("status", "cancelled")
                }) {
                    filter { eq("request_id", requestId) }
                }

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "cancelRequest failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteRequest(requestId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User not logged in"))

            client.from("food_requests").delete {
                filter {
                    eq("request_id", requestId)
                    eq("ngo_id", user.id)  // ✅ Changed from user_id
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "deleteRequest failed", e)
            Result.failure(e)
        }
    }
}
