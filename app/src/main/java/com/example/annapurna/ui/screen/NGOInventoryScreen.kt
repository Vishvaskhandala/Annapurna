package com.example.annapurna.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.viewmodel.NGOViewModel

private val Saffron = Color(0xFFFF6F00)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)
private val InTransitBlue = Color(0xFF1E88E5)
private val DeliveredGreen = Color(0xFF43A047)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NGOInventoryScreen(viewModel: NGOViewModel) {
    val claimedFood by viewModel.claimedFood.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchInitialData()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Inventory", fontWeight = FontWeight.Bold, color = DarkBrown) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmCream)
            )
        },
        containerColor = WarmCream
    ) { padding ->
        if (claimedFood.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inventory, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No active tasks found.", color = DarkBrown.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(claimedFood, key = { it.foodId }) { food ->
                    InventoryItemCard(
                        food = food,
                        onUpdateStatus = { newStatus -> viewModel.updateFoodStatus(food.foodId, newStatus) }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(food: FoodPost, onUpdateStatus: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = food.foodName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                StatusBadge(status = food.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = "Qty: ${food.quantity}", fontSize = 14.sp, color = DarkBrown.copy(alpha = 0.7f))
            Text(text = "📍 ${food.location}", fontSize = 13.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (food.status == "claimed_by_ngo") {
                    Button(
                        onClick = { onUpdateStatus("in_transit") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = InTransitBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pick Up", fontWeight = FontWeight.Bold)
                    }
                } else if (food.status == "in_transit") {
                    Button(
                        onClick = { onUpdateStatus("delivered") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DeliveredGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Mark Delivered", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "claimed_by_ngo" -> Saffron to "Claimed"
        "in_transit" -> InTransitBlue to "In Transit"
        "delivered" -> DeliveredGreen to "Delivered"
        else -> Color.Gray to status
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
