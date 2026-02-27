package com.example.annapurna.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.FoodViewModel

private val Saffron = Color(0xFFFF6F00)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)
private val EarthGreen = Color(0xFF33691E)

@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel = viewModel(),
    foodViewModel: FoodViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val availableFood by foodViewModel.availableFood.collectAsState()
    
    // Using filteredFood for better search/filter results
    val recentDonations = availableFood.take(10)
    val nearbyRequests = availableFood.filter { it.status == "available" }.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WarmCream),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Greeting Section
        item {
            HeaderSection(userName = currentUser?.name ?: "Donor")
        }

        // 2. Summary Stats
        item {
            SummaryStatsRow(donationsCount = recentDonations.size)
        }

        // 3. Nearby Requests Section
        item {
            SectionHeader(title = "Nearby Requests", action = "See All")
            NearbyRequestsRow(nearbyRequests)
        }

        // 4. Recent Donations Section
        item {
            SectionHeader(title = "Recent Donations", action = "View More")
        }

        items(recentDonations) { food ->
            RecentDonationCard(food)
        }
    }
}

@Composable
fun HeaderSection(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Namaste, $userName! 🙏", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DarkBrown)
            Text(text = "Sharing food is sharing love.", fontSize = 14.sp, color = DarkBrown.copy(alpha = 0.6f))
        }
        IconButton(
            onClick = { /* Handle Notifications */ },
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = Saffron)
        }
    }
}

@Composable
fun SummaryStatsRow(donationsCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatBox(modifier = Modifier.weight(1f), icon = "🍱", count = donationsCount.toString(), label = "Meals Shared", color = Saffron)
        StatBox(modifier = Modifier.weight(1f), icon = "🌱", count = "12", label = "Waste Reduced", color = EarthGreen)
    }
}

@Composable
fun StatBox(modifier: Modifier, icon: String, count: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 24.sp)
            Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 11.sp, color = DarkBrown.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
        TextButton(onClick = { /* Handle Action */ }) {
            Text(text = action, color = Saffron, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NearbyRequestsRow(requests: List<FoodPost>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(requests) { request ->
            RequestCard(request)
        }
    }
}

@Composable
fun RequestCard(request: FoodPost) {
    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Saffron.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Text("🥘", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = request.foodName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBrown)
                    Text(text = "📍 ${request.location}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Requires approx ${request.quantity} for pickup.", fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.7f), lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Handle Respond */ },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Help Now", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecentDonationCard(food: FoodPost) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = food.imageUrl.ifEmpty { "https://via.placeholder.com/150" },
                contentDescription = null,
                modifier = Modifier.size(68.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = food.foodName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBrown)
                Text(text = "Shared by ${food.donorName}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "📍 ${food.location}", fontSize = 11.sp, color = Saffron, fontWeight = FontWeight.Medium)
            }
            Text(text = "New", fontSize = 11.sp, color = EarthGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
        }
    }
}
