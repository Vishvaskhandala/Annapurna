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
import androidx.compose.ui.graphics.Brush
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
import com.example.annapurna.ui.navigation.Screen
import com.example.annapurna.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

private val Saffron = Color(0xFFFF6F00)
private val DeepSaffron = Color(0xFFE65100)
private val LightSaffron = Color(0xFFFFB300)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)
private val EarthGreen = Color(0xFF33691E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
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
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }
    val isDonor = currentUser?.userType == "donor"
    val isRefreshing by foodViewModel.isRefreshing.collectAsState()


    LaunchedEffect(Unit) { foodViewModel.startListeningIfLoggedIn() }

    LaunchedEffect(claimState) {
        when (claimState) {
            is ClaimState.Success -> { snackbarHostState.showSnackbar("🎉 Food claimed! Go pick it up."); foodViewModel.resetClaimState() }
            is ClaimState.Error -> { snackbarHostState.showSnackbar((claimState as ClaimState.Error).message); foodViewModel.resetClaimState() }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmCream,
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(colors = listOf(DeepSaffron, Saffron)))) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp).statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪔", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Annapurna", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 0.5.sp)
                        }
                        Text(if (isDonor) "Anna Daata Dashboard" else "Find Available Food",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center) {
                            Text(if (isDonor) "🍱" else "🤲", fontSize = 18.sp)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (isDonor) {
                FloatingActionButton(onClick = { navController.navigate(Screen.PostFood.route) },
                    containerColor = Saffron, contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.Add, "Post Food", modifier = Modifier.size(28.dp))
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    icon = { Text(if (selectedTab == 0) "🏠" else "🏡", fontSize = 20.sp) },
                    label = { Text(if (isDonor) "My Food" else "Available", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Saffron.copy(alpha = 0.12f)))
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    icon = { Text(if (selectedTab == 1) "🗺️" else "🗺️", fontSize = 20.sp) },
                    label = { Text("Map", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Saffron.copy(alpha = 0.12f)))
                NavigationBarItem(selected = selectedTab == 2, onClick = { navController.navigate(Screen.MyActivity.route) },
                    icon = { Text("📋", fontSize = 20.sp) },
                    label = { Text("Activity", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Saffron.copy(alpha = 0.12f)))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> if (isDonor) {
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
                1 -> MapViewPlaceholder()
            }
        }
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

    Box(modifier = Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Stats
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeStat(modifier = Modifier.weight(1f), "🍽️", donations.size.toString(), "Total", Saffron)
                HomeStat(modifier = Modifier.weight(1f), "✅", donations.count { it.status == "claimed" }.toString(), "Claimed", EarthGreen)
                HomeStat(modifier = Modifier.weight(1f), "⏳", donations.count { it.status == "available" }.toString(), "Open", Color(0xFF1565C0))
            }

            if (donations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("📦", fontSize = 72.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No donations yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap + to share your first food donation and change someone's day 🙏",
                                fontSize = 14.sp, color = DarkBrown.copy(alpha = 0.5f), textAlign = TextAlign.Center, lineHeight = 21.sp)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(donations, key = { it.foodId }) { food ->
                        DonorFoodCard(food = food, onDelete = { onDeleteFood(food.foodId) })
                    }
                }
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = WarmCream,
            contentColor = Saffron
        )
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

    Box(modifier = Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 🔍 Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search food, location...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            if (availableFood.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 72.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No matching food found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBrown)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableFood, key = { it.foodId }) { food ->
                        RecipientFoodCard(
                            food = food,
                            currentUserId = currentUserId,
                            isClaimLoading = claimState is ClaimState.Loading,
                            onClaim = { onClaimFood(food.foodId) }
                        )
                    }
                }
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = WarmCream,
            contentColor = Saffron
        )
    }
}

@Composable
fun DonorFoodCard(food: FoodPost, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)) {
        Column {
            if (food.imageUrl.isNotEmpty()) {
                AsyncImage(model = food.imageUrl, contentDescription = food.foodName,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Brush.horizontalGradient(colors = listOf(Saffron.copy(alpha = 0.2f), LightSaffron.copy(alpha = 0.2f)))),
                    contentAlignment = Alignment.Center) { Text("🍱", fontSize = 36.sp) }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(food.foodName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown, modifier = Modifier.weight(1f))
                    val (badgeColor, badgeText) = if (food.status == "available") Pair(EarthGreen, "Available") else Pair(Saffron, "Claimed")
                    Surface(color = badgeColor.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) {
                        Text(badgeText, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("📦 Qty: ${food.quantity}", fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.6f))
                Text("📍 ${food.location}", fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.6f))
                Text("⏰ Pickup: ${food.pickupTime}", fontSize = 13.sp, color = Saffron, fontWeight = FontWeight.Medium)
                Text("🕐 ${getTimeAgo(food.createdAt)}", fontSize = 12.sp, color = DarkBrown.copy(alpha = 0.4f))

                if (food.status == "available") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f))) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Post", fontSize = 14.sp)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post?", fontWeight = FontWeight.Bold) },
            text = { Text("This food post will be removed permanently.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = Saffron) } }
        )
    }
}

@Composable
fun RecipientFoodCard(food: FoodPost, currentUserId: String?, isClaimLoading: Boolean, onClaim: () -> Unit) {
    var showClaimDialog by remember { mutableStateOf(false) }
    val isAvailable = food.status == "available"
    val isClaimedByMe = food.claimedBy == currentUserId

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)) {
        Column {
            if (food.imageUrl.isNotEmpty()) {
                AsyncImage(model = food.imageUrl, contentDescription = food.foodName,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Brush.horizontalGradient(colors = listOf(Saffron.copy(alpha = 0.15f), LightSaffron.copy(alpha = 0.15f)))),
                    contentAlignment = Alignment.Center) { Text("🍱", fontSize = 36.sp) }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(food.foodName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                Spacer(modifier = Modifier.height(2.dp))
                Text("by ${food.donorName}", fontSize = 13.sp, color = Saffron, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("📦 ${food.quantity}", fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.6f))
                if (food.description.isNotEmpty()) Text("📝 ${food.description}", fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.6f))
                Text("📍 ${food.location}", fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.6f))
                Text("⏰ Pickup: ${food.pickupTime}", fontSize = 13.sp, color = Saffron, fontWeight = FontWeight.Medium)

                val hoursAgo = (System.currentTimeMillis() - food.createdAt) / (1000 * 60 * 60)
                val freshnessColor = when { hoursAgo < 2 -> EarthGreen; hoursAgo < 6 -> Saffron; else -> Color(0xFFD32F2F) }
                Text("🌿 ${getTimeAgo(food.createdAt)}", fontSize = 12.sp, color = freshnessColor, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isAvailable -> Button(onClick = { showClaimDialog = true }, modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isClaimLoading, shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron)) {
                        if (isClaimLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("🙏  Claim This Food", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    isClaimedByMe -> OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = false, shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EarthGreen)) {
                        Text("✅  You Claimed This", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    else -> OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = false, shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)) {
                        Text("Already Claimed", fontSize = 15.sp)
                    }
                }
            }
        }
    }

    if (showClaimDialog) {
        AlertDialog(onDismissRequest = { showClaimDialog = false },
            title = { Text("Claim This Food? 🙏", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("You're about to claim:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(food.foodName, fontWeight = FontWeight.Bold, color = Saffron, fontSize = 17.sp)
                    Text("📦 ${food.quantity}")
                    Text("⏰ ${food.pickupTime}")
                    Text("📍 ${food.location}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Please pick it up on time. This helps reduce waste 🌱",
                        fontSize = 12.sp, color = DarkBrown.copy(alpha = 0.6f))
                }
            },
            confirmButton = {
                Button(onClick = { onClaim(); showClaimDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron)) {
                    Text("Yes, I'll Pick It Up", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showClaimDialog = false }) { Text("Cancel", color = DarkBrown.copy(alpha = 0.5f)) } }
        )
    }
}

@Composable
fun HomeStat(modifier: Modifier, emoji: String, value: String, label: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 20.sp)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 11.sp, color = DarkBrown.copy(alpha = 0.5f))
        }
    }
}

fun getTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (1000 * 60); val hours = diff / (1000 * 60 * 60); val days = diff / (1000 * 60 * 60 * 24)
    return when {
        minutes < 1 -> "just now"; minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"; days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun MapViewPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("🗺️", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Map Coming Soon", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
            Spacer(modifier = Modifier.height(8.dp))
            Text("See food locations near you in the next update 🌱", fontSize = 14.sp,
            color = DarkBrown.copy(alpha = 0.5f), textAlign = TextAlign.Center, lineHeight = 21.sp)
        }
    }
}
