package com.example.annapurna.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.viewmodel.ClaimState
import com.example.annapurna.viewmodel.FoodViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.*

private val Saffron = Color(0xFFFF6F00)
private val DeepSaffron = Color(0xFFE65100)
private val LightSaffron = Color(0xFFFFB300)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)
private val EarthGreen = Color(0xFF33691E)

private val DEFAULT_LOCATION = LatLng(28.6139, 77.2090) // New Delhi

/** Returns distance in km between two LatLng points using Haversine formula */
fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun formatDistance(km: Double): String = when {
    km < 1.0 -> "${(km * 1000).toInt()} m"
    else -> String.format("%.1f km", km)
}

/** Freshness hue for Google Maps marker (green=fresh, orange=aging, red=old) */
fun freshnessHue(createdAt: Long): Float {
    val hoursAgo = (System.currentTimeMillis() - createdAt) / (1000 * 60 * 60)
    return when {
        hoursAgo < 2 -> BitmapDescriptorFactory.HUE_GREEN
        hoursAgo < 6 -> BitmapDescriptorFactory.HUE_ORANGE
        else -> BitmapDescriptorFactory.HUE_RED
    }
}

fun freshnessColor(createdAt: Long): Color {
    val hoursAgo = (System.currentTimeMillis() - createdAt) / (1000 * 60 * 60)
    return when {
        hoursAgo < 2 -> EarthGreen
        hoursAgo < 6 -> Saffron
        else -> Color(0xFFD32F2F)
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapView(
    foodViewModel: FoodViewModel,
    currentUserId: String? = null,
    claimState: ClaimState = ClaimState.Idle,
    onClaimFood: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val availableFood by foodViewModel.availableFood.collectAsState()
    val isRefreshing by foodViewModel.isRefreshing.collectAsState()

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedFood by remember { mutableStateOf<FoodPost?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showSheet by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_LOCATION, 10f)
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    fun fetchLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val newLocation = LatLng(it.latitude, it.longitude)
                userLocation = newLocation
                cameraPositionState.position = CameraPosition.fromLatLngZoom(newLocation, 13f)
            }
        }
    }

    // Auto-start live sync on map open
    LaunchedEffect(Unit) {
        foodViewModel.startRealtimeSync()
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            fetchLastLocation()
        }
    }

    // Dismiss sheet after successful claim
    LaunchedEffect(claimState) {
        if (claimState is ClaimState.Success) {
            showSheet = false
            selectedFood = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── MAP ──────────────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
            properties = MapProperties(isMyLocationEnabled = locationPermissionsState.allPermissionsGranted)
        ) {
            // User marker
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "You are here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // Food markers — color by freshness
            availableFood.forEach { food ->
                if (food.latitude != 0.0 || food.longitude != 0.0) {
                    val position = LatLng(food.latitude, food.longitude)
                    Marker(
                        state = MarkerState(position = position),
                        title = food.foodName,
                        snippet = "by ${food.donorName}",
                        icon = BitmapDescriptorFactory.defaultMarker(freshnessHue(food.createdAt)),
                        onClick = {
                            selectedFood = food
                            showSheet = true
                            false // return false to show default info window; true to suppress
                        }
                    )
                }
            }
        }

        // ── LIVE BADGE (top-left) ────────────────────────────────────────
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            color = if (isRefreshing) Saffron else EarthGreen,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Text(
                    if (isRefreshing) "Updating..." else "● Live",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── FOOD COUNT CHIP (top-right) ──────────────────────────────────
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            color = WarmCream,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 4.dp
        ) {
            Text(
                "🍱 ${availableFood.size} available",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBrown
            )
        }

        // ── LEGEND ───────────────────────────────────────────────────────
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 80.dp),
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Freshness", fontSize = 10.sp, color = DarkBrown.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                LegendRow("🟢", "< 2 hrs")
                LegendRow("🟠", "2–6 hrs")
                LegendRow("🔴", "> 6 hrs")
            }
        }

        // ── MY LOCATION FAB ──────────────────────────────────────────────
        FloatingActionButton(
            onClick = { fetchLastLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Saffron,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Center on me")
        }

        // ── PERMISSION OVERLAY ───────────────────────────────────────────
        if (!locationPermissionsState.allPermissionsGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = WarmCream
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("📍", fontSize = 40.sp)
                        Text("Location Needed", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBrown)
                        Text(
                            "Allow location access to see available food near you on the live map.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = DarkBrown.copy(alpha = 0.65f)
                        )
                        Button(
                            onClick = {
                                if (locationPermissionsState.shouldShowRationale) {
                                    locationPermissionsState.launchMultiplePermissionRequest()
                                } else {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = Uri.fromParts("package", (context as Activity).packageName, null)
                                    intent.data = uri
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Grant Permission", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ── FOOD DETAIL BOTTOM SHEET ─────────────────────────────────────────
    if (showSheet && selectedFood != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                selectedFood = null
            },
            sheetState = sheetState,
            containerColor = WarmCream,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            val food = selectedFood!!
            val distanceText = userLocation?.let {
                formatDistance(haversineDistance(it.latitude, it.longitude, food.latitude, food.longitude))
            }
            val hoursAgo = (System.currentTimeMillis() - food.createdAt) / (1000 * 60 * 60)
            val isClaimedByMe = food.claimedBy == currentUserId
            val isLoading = claimState is ClaimState.Loading

            Column(modifier = Modifier.padding(bottom = 32.dp)) {

                // Food image
                if (food.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.foodName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Saffron.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍱", fontSize = 48.sp)
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                    // Header row: name + freshness badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            food.foodName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkBrown,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = freshnessColor(food.createdAt).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                when {
                                    hoursAgo < 2 -> "🌿 Fresh"
                                    hoursAgo < 6 -> "⚡ Recent"
                                    else -> "⏳ Aging"
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = freshnessColor(food.createdAt)
                            )
                        }
                    }

                    Text("by ${food.donorName}", fontSize = 13.sp, color = Saffron, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info chips
                    InfoRow("📦", "Quantity", food.quantity)
                    if (food.description.isNotEmpty()) InfoRow("📝", "Note", food.description)
                    InfoRow("📍", "Location", food.location)
                    InfoRow("⏰", "Pickup", food.pickupTime)
                    distanceText?.let { InfoRow("🗺️", "Distance", it) }
                    InfoRow("🕐", "Posted", getTimeAgo(food.createdAt))

                    Spacer(modifier = Modifier.height(20.dp))

                    // Claim button
                    when {
                        isClaimedByMe -> OutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = false,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EarthGreen)
                        ) {
                            Text("✅  You Claimed This", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        food.status != "available" -> OutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = false,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Already Claimed", fontSize = 15.sp)
                        }

                        else -> Button(
                            onClick = { onClaimFood(food.foodId) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("🙏  Claim This Food", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(dot: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(dot, fontSize = 10.sp)
        Text(label, fontSize = 10.sp, color = DarkBrown.copy(alpha = 0.7f))
    }
}

@Composable
private fun InfoRow(icon: String, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Text("$icon  ", fontSize = 13.sp)
        Text("$label: ", fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = DarkBrown, modifier = Modifier.weight(1f))
    }
}