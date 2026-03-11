package com.example.annapurna.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.annapurna.MainActivity
import com.example.annapurna.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FirebaseService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val TAG = "FirebaseService"
        const val CHANNEL_IMPORTANT = "annapurna_important"
        const val CHANNEL_UPDATES = "annapurna_updates"
        const val CHANNEL_IMPACT = "annapurna_impact"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New token: $token")
        saveTokenToSupabase(token)
    }

    private fun saveTokenToSupabase(token: String) {
        scope.launch {
            try {
                val client = SupabaseClientProvider.client
                val userId = client.auth.currentUserOrNull()?.id
                if (userId == null) {
                    Log.w(TAG, "User not logged in, token not saved yet")
                    return@launch
                }
                client.from("users")
                    .update({ set("fcm_token", token) }) {
                        filter { eq("user_id", userId) }
                    }
                Log.d(TAG, "✅ Token saved for user: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to save token", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "📩 FCM received data: ${remoteMessage.data}")

        val type = remoteMessage.data["type"] ?: "default"
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Annapurna 🪔"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "You have a new update"
        val foodId = remoteMessage.data["food_id"]

        createNotificationChannels()
        showNotification(title, body, type, foodId)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val important = NotificationChannel(
                CHANNEL_IMPORTANT,
                "Important Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts like food claims and request matches"
                enableLights(true)
                enableVibration(true)
            }

            val updates = NotificationChannel(
                CHANNEL_UPDATES,
                "New Food & Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for nearby food and pickup reminders"
            }

            val impact = NotificationChannel(
                CHANNEL_IMPACT,
                "Impact & Gratitude",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thank you messages and impact updates"
            }

            manager.createNotificationChannels(listOf(important, updates, impact))
        }
    }

    private fun showNotification(title: String, body: String, type: String, foodId: String?) {
        val channelId = when (type) {
            "food_claimed", "request_fulfilled" -> CHANNEL_IMPORTANT
            "new_food", "pickup_reminder" -> CHANNEL_UPDATES
            "completed" -> CHANNEL_IMPACT
            else -> CHANNEL_UPDATES
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("type", type)
            putExtra("food_id", foodId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFFFF6F00.toInt()) // Saffron
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
