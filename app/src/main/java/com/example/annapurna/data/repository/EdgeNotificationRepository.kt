package com.example.annapurna.data.remote

import android.util.Log
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

    private val edgeUrl =
        "https://xxprnfckqgwocuxztigw.supabase.co/functions/v1/bright-task"

    companion object {
        private const val TAG = "EdgeNotification"
    }

    // 🔔 Notify recipients
    suspend fun sendNotificationToRecipients(
        foodName: String,
        donorName: String,
        location: String
    ) = withContext(Dispatchers.IO) {

        try {
            val accessToken = client.auth.currentAccessTokenOrNull()

            if (accessToken == null) {
                Log.e(TAG, "❌ No access token. User not logged in.")
                return@withContext
            }

            val json = JSONObject().apply {
                put("type", "NEW_FOOD")
                put("foodName", foodName)
                put("donorName", donorName)
                put("location", location)
            }

            sendRequest(json, accessToken)

        } catch (e: Exception) {
            Log.e(TAG, "Recipients notification failed", e)
        }
    }

    // 🔔 Notify donor
    suspend fun sendNotificationToDonor(
        donorId: String,
        recipientName: String,
        foodName: String
    ) = withContext(Dispatchers.IO) {

        try {
            val accessToken = client.auth.currentAccessTokenOrNull()

            if (accessToken == null) {
                Log.e(TAG, "❌ No access token. User not logged in.")
                return@withContext
            }

            val json = JSONObject().apply {
                put("type", "FOOD_CLAIMED")
                put("donorId", donorId)
                put("recipientName", recipientName)
                put("foodName", foodName)
            }

            sendRequest(json, accessToken)

        } catch (e: Exception) {
            Log.e(TAG, "Donor notification failed", e)
        }
    }

    // 🌐 Core request
    private fun sendRequest(json: JSONObject, accessToken: String) {

        try {
            Log.d(TAG, "➡️ Calling Edge Function...")
            Log.d(TAG, "Payload: $json")

            val requestBody =
                json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(edgeUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken") // ⭐ FIX
                .build()

            val response = httpClient.newCall(request).execute()

            Log.d(TAG, "✅ Edge response code: ${response.code}")
            Log.d(TAG, "📥 Edge response body: ${response.body?.string()}")

            response.close()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Edge request error", e)
        }
    }
}
