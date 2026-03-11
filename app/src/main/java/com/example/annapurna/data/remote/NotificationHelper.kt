package com.example.annapurna.data.remote

import android.util.Log
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    // ── Get FCM token of a user from Supabase ──
    private suspend fun getFcmToken(userId: String): String? {
        return try {
            val client = SupabaseClientProvider.client
            val result = client.from("users")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Map<String, String>>()

            result.firstOrNull()?.get("fcm_token")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }

    // ── When food is CLAIMED → notify Donor ──
    suspend fun notifyDonorFoodClaimed(
        donorId: String,
        recipientName: String,
        foodName: String
    ) {
        val token = getFcmToken(donorId) ?: return
        sendFcmNotification(
            fcmToken = token,
            title = "🎉 Your food was claimed!",
            body = "$recipientName just claimed your \"$foodName\". Get ready for pickup!"
        )
    }

    // ── When food is POSTED → notify all recipients ──
    suspend fun notifyRecipientsNewFood(
        foodName: String,
        donorName: String,
        location: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val client = SupabaseClientProvider.client

                // Fetch all recipient tokens
                val recipients = client.from("users")
                    .select {
                        filter { eq("user_type", "recipient") }
                    }
                    .decodeList<Map<String, String>>()

                recipients.forEach { user ->
                    val token = user["fcm_token"] ?: return@forEach
                    if (token.isBlank()) return@forEach

                    sendFcmNotification(
                        fcmToken = token,
                        title = "🍱 New Food Available!",
                        body = "$donorName shared \"$foodName\" near $location"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to notify recipients", e)
            }
        }
    }

    // ── Core FCM sender using Legacy HTTP API ──
    // Replace YOUR_SERVER_KEY with key from Firebase Console → Project Settings → Cloud Messaging
    private suspend fun sendFcmNotification(
        fcmToken: String,
        title: String,
        body: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val serverKey = "YOUR_SERVER_KEY" // 🔴 Replace this

                val url = URL("https://fcm.googleapis.com/fcm/send")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "key=$serverKey")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val payload = """
                    {
                        "to": "$fcmToken",
                        "notification": {
                            "title": "$title",
                            "body": "$body",
                            "sound": "default"
                        },
                        "priority": "high"
                    }
                """.trimIndent()

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(payload)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                Log.d(TAG, "FCM Response: $responseCode")
                connection.disconnect()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to send FCM", e)
            }
        }
    }
}