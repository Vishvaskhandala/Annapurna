package com.example.annapurna.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.annapurna.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- Reusable Color Palette ---
private val Saffron = Color(0xFFFF6F00)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)
private val EarthGreen = Color(0xFF33691E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDonationsScreen(
    foodViewModel: FoodViewModel = viewModel()
) {
    val myDonations by foodViewModel.myDonations.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Donations", fontWeight = FontWeight.Bold, color = DarkBrown) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmCream)
            )
        },
        containerColor = WarmCream
    ) { padding ->
        if (myDonations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("You haven\'t made any donations yet. 😢", color = DarkBrown.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(myDonations, key = { it.foodId }) { donation ->
                    DonationItemCard(
                        donation = donation,
                        onDelete = { foodViewModel.deleteFood(donation.foodId) },
                        onEdit = { /* TODO: Navigate to Edit Screen */ }
                    )
                }
            }
        }
    }
}

@Composable
fun DonationItemCard(
    donation: FoodPost,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Image Section
            AsyncImage(
                model = donation.imageUrl.ifEmpty { "https://via.placeholder.com/150/FFC107/000000?Text=Food" },
                contentDescription = donation.foodName,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Info Section
            Column(modifier = Modifier.weight(1f)) {
                Text(text = donation.foodName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                Spacer(modifier = Modifier.height(4.dp))
                // Using getTimeAgo from the package level (HomeScreen.kt)
                Text(text = "Posted ${getTimeAgo(donation.createdAt)}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                DonationStatusBadge(status = donation.status)
            }

            // Actions Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = {
                        onEdit()
                        showMenu = false
                    })
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DonationStatusBadge(status: String){
    val (color, text) = when (status.lowercase()) {
        "available" -> EarthGreen to "Open"
        "claimed" -> Saffron to "Claimed"
        else -> Color.Gray to "Expired"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 0.5.sp
        )
    }
}
