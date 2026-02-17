package com.example.annapurna.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        const val CHANNEL_ID = "annapurna_channel"
        const val CHANNEL_NAME = "Annapurna Food Notifications"
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

        Log.d(TAG, "📩 FCM received from: ${remoteMessage.from}")
        Log.d(TAG, "   notification: ${remoteMessage.notification}")
        Log.d(TAG, "   data: ${remoteMessage.data}")

        val title = remoteMessage.data["title"]
            ?: remoteMessage.notification?.title
            ?: "Annapurna 🪔"

        val body = remoteMessage.data["body"]
            ?: remoteMessage.notification?.body
            ?: "You have a new update"

        Log.d(TAG, "   Showing: title=$title, body=$body")

        // ✅ Always create channel before showing
        createNotificationChannel()
        showNotification(title, body)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Food donation and claim notifications"
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                    // ✅ Vivo fix: explicit vibration pattern
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                    // ✅ Show on lockscreen
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                manager.createNotificationChannel(channel)
                Log.d(TAG, "✅ Notification channel created")
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        // ✅ Check if notifications are enabled - catches Vivo system block
        val notifManager = NotificationManagerCompat.from(this)
        if (!notifManager.areNotificationsEnabled()) {
            Log.e(TAG, "❌ Notifications DISABLED by system for this app!")
            Log.e(TAG, "   Fix: Settings → Apps → Annapurna → Notifications → Enable")
            return
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // ✅ show on lockscreen
            .build()

        val notifId = System.currentTimeMillis().toInt()
        manager.notify(notifId, notification)
        Log.d(TAG, "✅ Notification shown with id: $notifId")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}






















































