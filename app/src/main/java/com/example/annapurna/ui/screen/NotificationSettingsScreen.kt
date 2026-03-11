package com.example.annapurna.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val Saffron = Color(0xFFFF6F00)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("annapurna_notifications", Context.MODE_PRIVATE) }

    var foodClaimed by remember { mutableStateOf(sharedPrefs.getBoolean("food_claimed", true)) }
    var newFoodNearby by remember { mutableStateOf(sharedPrefs.getBoolean("new_food", true)) }
    var requestMatches by remember { mutableStateOf(sharedPrefs.getBoolean("request_matches", true)) }
    var pickupReminders by remember { mutableStateOf(sharedPrefs.getBoolean("pickup_reminders", true)) }
    var impactUpdates by remember { mutableStateOf(sharedPrefs.getBoolean("impact_updates", true)) }
    var distanceRange by remember { mutableFloatStateOf(sharedPrefs.getFloat("distance_range", 5f)) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notification Settings", fontWeight = FontWeight.Bold, color = DarkBrown) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkBrown)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmCream)
            )
        },
        containerColor = WarmCream
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Control what updates you receive:", fontSize = 14.sp, color = Color.Gray)

            SettingsCard {
                Column {
                    NotificationToggle("Food Claimed", "When someone claims your donation", foodClaimed) {
                        foodClaimed = it
                        sharedPrefs.edit().putBoolean("food_claimed", it).apply()
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = WarmCream)
                    NotificationToggle("New Food Nearby", "Alerts for surplus food in your area", newFoodNearby) {
                        newFoodNearby = it
                        sharedPrefs.edit().putBoolean("new_food", it).apply()
                    }
                    
                    if (newFoodNearby) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Radius: ${distanceRange.toInt()} km", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Saffron)
                        Slider(
                            value = distanceRange,
                            onValueChange = { 
                                distanceRange = it
                                sharedPrefs.edit().putFloat("distance_range", it).apply()
                            },
                            valueRange = 1f..20f,
                            steps = 3,
                            colors = SliderDefaults.colors(thumbColor = Saffron, activeTrackColor = Saffron)
                        )
                    }
                }
            }

            SettingsCard {
                Column {
                    NotificationToggle("Request Matches", "When food matches your active requests", requestMatches) {
                        requestMatches = it
                        sharedPrefs.edit().putBoolean("request_matches", it).apply()
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = WarmCream)
                    NotificationToggle("Pickup Reminders", "Alerts 1 hour before scheduled time", pickupReminders) {
                        pickupReminders = it
                        sharedPrefs.edit().putBoolean("pickup_reminders", it).apply()
                    }
                }
            }

            SettingsCard {
                NotificationToggle("Impact Updates", "Summaries of your contribution", impactUpdates) {
                    impactUpdates = it
                    sharedPrefs.edit().putBoolean("impact_updates", it).apply()
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun NotificationToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBrown)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Saffron)
        )
    }
}
