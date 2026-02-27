package com.example.annapurna.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.annapurna.viewmodel.ClaimState
import com.example.annapurna.viewmodel.FoodViewModel

private val Saffron = Color(0xFFFF6F00)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)
private val EarthGreen = Color(0xFF33691E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailsScreen(
    navController: NavController,
    foodViewModel: FoodViewModel,
    foodId: String
) {
    val foodPost = remember { foodViewModel.getFoodById(foodId) }
    val claimState by foodViewModel.claimState.collectAsState()

    if (foodPost == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Food item not found.")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = WarmCream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Food Image
            AsyncImage(
                model = foodPost.imageUrl.ifEmpty { "https://via.placeholder.com/600x400/FFC107/FFFFFF?text=${foodPost.foodName}" },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = foodPost.foodName, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = DarkBrown)
                    Surface(color = EarthGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = foodPost.status.uppercase(),
                            color = EarthGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(text = "by ${foodPost.donorName}", fontSize = 15.sp, color = Saffron, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(24.dp))

                InfoRow(icon = Icons.Default.Inventory, label = "Quantity", value = foodPost.quantity)
                InfoRow(icon = Icons.Default.Schedule, label = "Pickup Time", value = foodPost.pickupTime)
                InfoRow(icon = Icons.Default.LocationOn, label = "Location", value = foodPost.location)

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (foodPost.description.isEmpty()) "No description provided." else foodPost.description,
                    fontSize = 15.sp,
                    color = DarkBrown.copy(alpha = 0.7f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                val isClaiming = claimState is ClaimState.Loading && (claimState as ClaimState.Loading).foodId == foodId

                Button(
                    onClick = { foodViewModel.claimFood(foodId) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(16.dp),
                    enabled = foodPost.status == "available" && !isClaiming
                ) {
                    if (isClaiming) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (foodPost.status == "available") "Claim This Meal" else "Already Claimed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Saffron, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = "$label:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBrown.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, fontSize = 14.sp, color = DarkBrown)
    }
}
