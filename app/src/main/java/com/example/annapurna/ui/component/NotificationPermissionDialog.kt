package com.example.annapurna.ui.component

import android.Manifest
import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionDialog(
    onDismiss: () -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        onDismiss()
        return
    }

    val notificationPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )

    if (!notificationPermissionState.status.isGranted) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Stay Updated 🔔", fontWeight = FontWeight.Bold) },
            text = { 
                Text("Get notified when your food is claimed or when food is available nearby to help reduce waste.") 
            },
            confirmButton = {
                Button(
                    onClick = { 
                        notificationPermissionState.launchPermissionRequest()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
                ) {
                    Text("Allow Notifications")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Maybe Later", color = Color.Gray)
                }
            }
        )
    } else {
        onDismiss()
    }
}
