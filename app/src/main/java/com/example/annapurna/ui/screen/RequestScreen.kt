package com.example.annapurna.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.annapurna.viewmodel.RequestState
import com.example.annapurna.viewmodel.RequestViewModel
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

private val Saffron = Color(0xFFFF6F00)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestScreen(navController: NavController, viewModel: RequestViewModel = viewModel()) {
    var foodType by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("Normal") }
    var neededBy by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    var errorMessage by remember { mutableStateOf("") }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val requestState by viewModel.createRequestState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    // Date Picker Logic
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            neededBy = formatter.format(selectedDate.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // Permission granted, fetch location
            fetchCurrentLocation(
                fusedLocationClient = fusedLocationClient,
                geocoder = geocoder,
                onLocationFetched = { lat, lng, address ->
                    latitude = lat
                    longitude = lng
                    locationName = address
                    isFetchingLocation = false
                },
                onError = {
                    locationName = "Unable to get location"
                    isFetchingLocation = false
                }
            )
        } else {
            // Permission denied
            isFetchingLocation = false
            errorMessage = "Location permission required to use current location"
        }
    }

    LaunchedEffect(requestState) {
        if (requestState is RequestState.Success) {
            viewModel.resetCreateState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Food", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Saffron)
            )
        },
        containerColor = WarmCream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Tell us what you need 🙏", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown)

            OutlinedTextField(
                value = foodType,
                onValueChange = { foodType = it; errorMessage = "" },
                label = { Text("Food Needed (e.g. Rice, Dal)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    focusedLabelColor = Saffron,
                    cursorColor = Saffron
                )
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it; errorMessage = "" },
                label = { Text("Quantity (e.g. 50 servings, 10 kg)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    focusedLabelColor = Saffron,
                    cursorColor = Saffron
                )
            )

            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it; errorMessage = "" },
                label = { Text("Purpose (e.g. Community Lunch)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    focusedLabelColor = Saffron,
                    cursorColor = Saffron
                )
            )

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it; errorMessage = "" },
                label = { Text("Delivery Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    focusedLabelColor = Saffron,
                    cursorColor = Saffron
                ),
                trailingIcon = {
                    if (isFetchingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Saffron
                        )
                    } else {
                        IconButton(onClick = {
                            isFetchingLocation = true
                            errorMessage = ""

                            // Check if permission is already granted
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) -> {
                                    // Permission already granted
                                    fetchCurrentLocation(
                                        fusedLocationClient = fusedLocationClient,
                                        geocoder = geocoder,
                                        onLocationFetched = { lat, lng, address ->
                                            latitude = lat
                                            longitude = lng
                                            locationName = address
                                            isFetchingLocation = false
                                        },
                                        onError = {
                                            locationName = "Unable to get location"
                                            isFetchingLocation = false
                                        }
                                    )
                                }
                                else -> {
                                    // Request permission
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        }) {
                            Icon(Icons.Default.MyLocation, "Get current location", tint = Saffron)
                        }
                    }
                }
            )

            OutlinedTextField(
                value = neededBy,
                onValueChange = { },
                label = { Text("Needed By Date") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    focusedLabelColor = Saffron,
                    cursorColor = Saffron
                ),
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = Saffron)
                    }
                }
            )

            Text("Urgency", fontWeight = FontWeight.Bold, color = DarkBrown, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Normal", "Urgent", "Immediate").forEach { level ->
                    FilterChip(
                        selected = urgency == level,
                        onClick = { urgency = level },
                        label = { Text(level) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Saffron.copy(alpha = 0.2f),
                            selectedLabelColor = Saffron
                        )
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        errorMessage,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (requestState is RequestState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        (requestState as RequestState.Error).message,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    when {
                        foodType.isBlank() -> errorMessage = "Please enter food type"
                        quantity.isBlank() -> errorMessage = "Please enter quantity"
                        purpose.isBlank() -> errorMessage = "Please enter purpose"
                        locationName.isBlank() -> errorMessage = "Please enter location"
                        neededBy.isBlank() -> errorMessage = "Please select a date"
                        else -> {
                            errorMessage = ""
                            viewModel.createRequest(
                                foodType = foodType.trim(),
                                quantity = quantity.trim(),
                                urgency = urgency,
                                purpose = purpose.trim(),
                                location = locationName.trim(),
                                latitude = latitude,
                                longitude = longitude,
                                neededBy = neededBy
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Saffron,
                    disabledContainerColor = Saffron.copy(alpha = 0.5f)
                ),
                enabled = requestState !is RequestState.Loading
            ) {
                if (requestState is RequestState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("🙏 Submit Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helper function to fetch location with proper permission handling
@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    geocoder: Geocoder,
    onLocationFetched: (Double, Double, String) -> Unit,
    onError: () -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    listOfNotNull(
                        addr.featureName,
                        addr.locality,
                        addr.subAdminArea,
                        addr.adminArea
                    ).take(3).joinToString(", ")
                } else {
                    "Lat: ${location.latitude}, Lon: ${location.longitude}"
                }
                onLocationFetched(location.latitude, location.longitude, address)
            } catch (e: Exception) {
                onLocationFetched(
                    location.latitude,
                    location.longitude,
                    "Lat: ${location.latitude}, Lon: ${location.longitude}"
                )
            }
        } else {
            onError()
        }
    }.addOnFailureListener {
        onError()
    }
}