package com.example.annapurna.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.ClaimState
import com.example.annapurna.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

private val Saffron = Color(0xFFFF6F00)
private val EarthGreen = Color(0xFF33691E)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    foodViewModel: FoodViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val availableFood by foodViewModel.filteredFood.collectAsState()
    val searchQuery by foodViewModel.searchQuery.collectAsState()
    val myDonations by foodViewModel.myDonations.collectAsState()
    val claimState by foodViewModel.claimState.collectAsState()
    val isRefreshing by foodViewModel.isRefreshing.collectAsState()
    val isDonor = currentUser?.userType == "donor"

    LaunchedEffect(Unit) { foodViewModel.startListeningIfLoggedIn() }

    if (isDonor) {
        DonorDashboard(
            donations = myDonations,
            onDeleteFood = { foodViewModel.deleteFood(it) },
            isRefreshing = isRefreshing,
            onRefresh = { foodViewModel.refreshData() }
        )
    } else {
        RecipientDashboard(
            availableFood = availableFood,
            currentUserId = currentUser?.userId,
            claimState = claimState,
            onClaimFood = { foodViewModel.claimFood(it) },
            searchQuery = searchQuery,
            onSearchChange = { foodViewModel.updateSearchQuery(it) },
            isRefreshing = isRefreshing,
            onRefresh = { foodViewModel.refreshData() }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DonorDashboard(
    donations: List<FoodPost>,
    onDeleteFood: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)

    Box(modifier = Modifier.fillMaxSize().background(WarmCream).pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary Stats for Donor
            SummaryHeader(donationsCount = donations.size)

            if (donations.isEmpty()) {
                EmptyState(message = "You haven't shared any meals yet. 🙏")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(donations, key = { it.foodId }) { food ->
                        // Reuse the refined DonationItemCard from MyDonationsScreen logic if possible, 
                        // or define a dashboard-specific one.
                        Text(text = food.foodName, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
        PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipientDashboard(
    availableFood: List<FoodPost>,
    currentUserId: String?,
    claimState: ClaimState,
    onClaimFood: (String) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)

    Box(modifier = Modifier.fillMaxSize().background(WarmCream).pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Search Bar
            SearchBar(query = searchQuery, onQueryChange = onSearchChange)

            if (availableFood.isEmpty()) {
                EmptyState(message = "No food available right now. 🥗")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(availableFood, key = { it.foodId }) { food ->
                        RecipientFoodCard(
                            food = food,
                            isClaimLoading = claimState is ClaimState.Loading && (claimState as ClaimState.Loading).foodId == food.foodId,
                            onClaim = { onClaimFood(food.foodId) }
                        )
                    }
                }
            }
        }
        PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        placeholder = { Text("Search for food or location...") },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Saffron) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Saffron,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        )
    )
}

@Composable
fun RecipientFoodCard(
    food: FoodPost,
    isClaimLoading: Boolean,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Food Image
            AsyncImage(
                model = food.imageUrl.ifEmpty { "https://via.placeholder.com/400x200/FFC107/FFFFFF?text=${food.foodName}" },
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = food.foodName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkBrown)
                        Text(text = "by ${food.donorName}", fontSize = 13.sp, color = Saffron, fontWeight = FontWeight.Medium)
                    }
                    Surface(color = EarthGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(text = "FRESH", color = EarthGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = food.location, fontSize = 13.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClaim,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isClaimLoading
                ) {
                    if (isClaimLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
                    } else {
                        Text("Claim This Meal", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryHeader(donationsCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeStat(Modifier.weight(1f), "🍽️", donationsCount.toString(), "Total", Saffron)
        HomeStat(Modifier.weight(1f), "🌿", "12kg", "Waste Saved", EarthGreen)
    }
}

@Composable
fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = DarkBrown.copy(alpha = 0.5f), fontSize = 16.sp)
    }
}

@Composable
fun HomeStat(modifier: Modifier, emoji: String, value: String, label: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 11.sp, color = DarkBrown.copy(alpha = 0.5f))
        }
    }
}

fun getTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (1000 * 60); val hours = diff / (1000 * 60 * 60); val days = diff / (1000 * 60 * 60 * 24)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
