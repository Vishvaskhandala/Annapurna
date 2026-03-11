package com.example.annapurna.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.ClaimState
import com.example.annapurna.viewmodel.FoodViewModel
import com.example.annapurna.viewmodel.CompletionState
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
    val completionState by foodViewModel.completionState.collectAsState()
    val isDonor = currentUser?.userType == "donor"

    LaunchedEffect(Unit) { foodViewModel.startListeningIfLoggedIn() }

    LaunchedEffect(completionState) {
        if (completionState is CompletionState.Success) {
            foodViewModel.resetCompletionState()
            authViewModel.refreshUser()
        }
    }

    if (isDonor) {
        DonorDashboard(
            donations = myDonations,
            onDeleteFood = { foodViewModel.deleteFood(it) },
            onMarkCompleted = { foodViewModel.markAsCompleted(it) },
            isRefreshing = isRefreshing,
            onRefresh = { foodViewModel.refreshData() }
        )
    } else {
        RecipientDashboard(
            availableFood = availableFood,
            currentUserId = currentUser?.userId,
            claimState = claimState,
            onClaimFood = { foodViewModel.claimFood(it) },
            onMarkCompleted = { foodViewModel.markAsCompleted(it) },
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
    onMarkCompleted: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)

    Box(modifier = Modifier.fillMaxSize().background(WarmCream).pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize()) {
            SummaryHeader(donationsCount = donations.count { it.status == "completed" })

            if (donations.isEmpty()) {
                EmptyState(message = "You haven't shared any meals yet. 🙏\nClick the + button below to start donating!")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(donations, key = { it.foodId }) { food ->
                        DonorFoodCard(
                            food = food,
                            onDelete = { onDeleteFood(food.foodId) },
                            onMarkCompleted = { onMarkCompleted(food.foodId) }
                        )
                    }
                }
            }
        }
        PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
fun DonorFoodCard(
    food: FoodPost,
    onDelete: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Donation?") },
            text = { Text("Are you sure you want to remove '${food.foodName}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Confirm Delivery?") },
            text = { Text("Confirm that the recipient has picked up '${food.foodName}'?") },
            confirmButton = {
                Button(
                    onClick = { onMarkCompleted(); showCompleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                ) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showCompleteDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = food.imageUrl.ifEmpty { "https://via.placeholder.com/400x200/FFC107/FFFFFF?text=${food.foodName}" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
                    color = when(food.status) {
                        "completed" -> Color(0xFFE8F5E9)
                        "claimed" -> Saffron
                        else -> EarthGreen
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (food.status == "completed") "✅ COMPLETED" else food.status.uppercase(),
                        color = if (food.status == "completed") EarthGreen else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = food.foodName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                    Text(text = getTimeAgo(food.createdAt), fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (food.status == "claimed") {
                    Button(
                        onClick = { showCompleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark as Delivered", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = Saffron, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = food.quantity, fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Saffron, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = food.location, fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.7f), maxLines = 1)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipientDashboard(
    availableFood: List<FoodPost>,
    currentUserId: String?,
    claimState: ClaimState,
    onClaimFood: (String) -> Unit,
    onMarkCompleted: (String) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)

    Box(modifier = Modifier.fillMaxSize().background(WarmCream).pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                            currentUserId = currentUserId,
                            isClaimLoading = claimState is ClaimState.Loading && (claimState as ClaimState.Loading).foodId == food.foodId,
                            onClaim = { onClaimFood(food.foodId) },
                            onConfirmPickup = { onMarkCompleted(food.foodId) }
                        )
                    }
                }
            }
        }
        PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
fun RecipientFoodCard(
    food: FoodPost,
    currentUserId: String?,
    isClaimLoading: Boolean,
    onClaim: () -> Unit,
    onConfirmPickup: () -> Unit
) {
    val isClaimedByMe = food.claimedBy == currentUserId
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Pickup?") },
            text = { Text("Confirm that you have picked up '${food.foodName}' from the donor?") },
            confirmButton = {
                Button(onClick = { onConfirmPickup(); showConfirmDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = EarthGreen)) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
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
                    if (food.status == "completed") {
                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                            Text(text = "COMPLETED", color = EarthGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                if (isClaimedByMe && food.status == "claimed") {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EarthGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Pickup", fontWeight = FontWeight.Bold)
                    }
                } else if (food.status == "available") {
                    Button(
                        onClick = onClaim,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isClaimLoading
                    ) {
                        if (isClaimLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        else Text("Claim This Meal", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (food.status == "claimed") {
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("Already Claimed") }
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
        HomeStat(Modifier.weight(1f), "🍽️", donationsCount.toString(), "Delivered", Saffron)
        HomeStat(Modifier.weight(1f), "🌿", "${donationsCount * 2}kg", "Waste Saved", EarthGreen)
    }
}

@Composable
fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("🍱", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "No donations yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = DarkBrown.copy(alpha = 0.6f), fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        }
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
