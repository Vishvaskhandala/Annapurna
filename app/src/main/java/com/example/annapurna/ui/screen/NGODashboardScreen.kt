package com.example.annapurna.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.data.model.FoodRequest
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.FoodViewModel
import com.example.annapurna.viewmodel.RequestViewModel

private val Saffron = Color(0xFFFF6F00)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)
private val EarthGreen = Color(0xFF33691E)

@Composable
fun NGODashboardScreen(
    authViewModel: AuthViewModel,
    foodViewModel: FoodViewModel,
    requestViewModel: RequestViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val availableFood by foodViewModel.availableFood.collectAsState()
    val myRequests by requestViewModel.myRequests.collectAsState()
    
    val claimState by foodViewModel.claimState.collectAsState()

    LaunchedEffect(Unit) {
        foodViewModel.refreshData()
        requestViewModel.loadMyRequests()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WarmCream),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. NGO Header
        item {
            NGOHeader(name = currentUser?.name ?: "NGO")
        }

        // 2. Stats Section
        item {
            NGOStatsSection(
                activeRequests = myRequests.filter { it.status == "open" }.size,
                foodReceived = currentUser?.foodReceived ?: 0,
                peopleHelped = (currentUser?.foodReceived ?: 0) * 5 // Mock multiplier
            )
        }

        // 3. Incoming Offers (Nearby Food)
        item {
            SectionHeader(title = "Available Food Nearby", action = "View Map")
        }

        if (availableFood.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No food available nearby", color = Color.Gray)
                }
            }
        } else {
            items(availableFood.take(5)) { food ->
                RecipientFoodCard(
                    food = food,
                    currentUserId = currentUser?.userId,
                    isClaimLoading = false, 
                    onClaim = { foodViewModel.claimFood(food.foodId) },
                    onConfirmPickup = { foodViewModel.markAsCompleted(food.foodId) }
                )
            }
        }

        // 4. My Active Requests
        item {
            SectionHeader(title = "My Food Requests", action = "New Request")
        }

        if (myRequests.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("You haven't posted any requests", color = Color.Gray)
                }
            }
        } else {
            items(myRequests.take(3)) { request ->
                NGORequestCard(request)
            }
        }
    }
}

@Composable
fun NGOHeader(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Hello, $name! 🏢", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DarkBrown)
            Text(text = "Manage your donations and requests.", fontSize = 14.sp, color = DarkBrown.copy(alpha = 0.6f))
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.Notifications, null, tint = Saffron)
        }
    }
}

@Composable
fun NGOStatsSection(activeRequests: Int, foodReceived: Int, peopleHelped: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NGOStatBox(modifier = Modifier.weight(1f), icon = "🔴", count = activeRequests.toString(), label = "Active Req")
        NGOStatBox(modifier = Modifier.weight(1f), icon = "📦", count = foodReceived.toString(), label = "Received")
        NGOStatBox(modifier = Modifier.weight(1f), icon = "🙏", count = peopleHelped.toString(), label = "Helped")
    }
}

@Composable
fun NGOStatBox(modifier: Modifier, icon: String, count: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Text(text = count, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
            Text(text = label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun NGORequestCard(request: FoodRequest) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Saffron.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (request.urgency == "High") "🔥" else "🥣")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = request.foodType, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Status: ${request.status}", fontSize = 12.sp, color = Saffron)
            }
            Surface(
                color = if (request.status == "open") EarthGreen.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = request.status.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (request.status == "open") EarthGreen else Color.Gray
                )
            }
        }
    }
}
