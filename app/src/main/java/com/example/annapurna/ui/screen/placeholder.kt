package com.example.annapurna.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MyDonationsScreen(navController: NavController) {
    Box(modifier = Modifier.padding(16.dp)) {
        Text("My Donations Screen - Coming in Step 4")
    }
}

@Composable
fun CommunityScreen(navController: NavController) {
    Box(modifier = Modifier.padding(16.dp)) {
        Text("Community Screen - Coming in Step 5")
    }
}
