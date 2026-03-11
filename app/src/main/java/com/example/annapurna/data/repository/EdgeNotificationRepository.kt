package com.example.annapurna.data.repository

import android.util.Log
import com.example.annapurna.data.remote.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class EdgeNotificationRepository {

    private val client: SupabaseClient = SupabaseClientProvider.client
    private val httpClient = OkHttpClient()

    private val edgeUrl = "https://xxprnfckqgwocuxztigw.supabase.co/functions/v1/bright-task"

    companion object {
        private const val TAG = "EdgeNotification"
    }

    suspend fun sendNotificationToRecipients(
        foodName: String,
        donorName: String,
        location: String,
        foodId: String
    ) = withContext(Dispatchers.IO) {
        try {
            val accessToken = client.auth.currentAccessTokenOrNull() ?: return@withContext
            val json = JSONObject().apply {
                put("type", "new_food")
                put("foodName", foodName)
                put("donorName", donorName)
                put("location", location)
                put("food_id", foodId)
            }
            sendRequest(json, accessToken)
        } catch (e: Exception) {
            Log.e(TAG, "Recipients notification failed", e)
        }
    }

    suspend fun sendNotificationToDonor(
        donorId: String,
        recipientName: String,
        foodName: String,
        foodId: String
    ) = withContext(Dispatchers.IO) {
        try {
            val accessToken = client.auth.currentAccessTokenOrNull() ?: return@withContext
            val json = JSONObject().apply {
                put("type", "food_claimed")
                put("donorId", donorId)
                put("recipientName", recipientName)
                put("foodName", foodName)
                put("food_id", foodId)
            }
            sendRequest(json, accessToken)
        } catch (e: Exception) {
            Log.e(TAG, "Donor notification failed", e)
        }
    }

    private fun sendRequest(json: JSONObject, accessToken: String) {
        try {
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(edgeUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            httpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "Edge response: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Edge request error", e)
        }
    }
}
