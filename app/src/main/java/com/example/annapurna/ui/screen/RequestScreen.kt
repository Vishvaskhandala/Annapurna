package com.example.annapurna.ui.screen

import android.Manifest
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.annapurna.viewmodel.RequestState
import com.example.annapurna.viewmodel.RequestViewModel
import com.google.android.gms.location.LocationServices
import java.util.*

private val Saffron = Color(0xFFFF6F00)
private val DeepSaffron = Color(0xFFE65100)
private val LightSaffron = Color(0xFFFFB300)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestScreen(navController: NavController, viewModel: RequestViewModel = viewModel()) {
    var foodNeeded by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("Normal") }
    var errorMessage by remember { mutableStateOf("") }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val requestState by viewModel.requestState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            isFetchingLocation = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        locationName = addresses?.firstOrNull()?.getAddressLine(0) ?: "Lat: ${location.latitude}, Lon: ${location.longitude}"
                    } catch (e: Exception) {
                        locationName = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                    }
                }
                isFetchingLocation = false
            }
        }
    }

    LaunchedEffect(requestState) {
        if (requestState is RequestState.Success) {
            viewModel.resetState()
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
                value = foodNeeded,
                onValueChange = { foodNeeded = it },
                label = { Text("Food Needed") },
                placeholder = { Text("e.g. Rice, Dal, Bread") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                placeholder = { Text("e.g. 5 people, 2 kg") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Delivery Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (isFetchingLocation) {
                        CircularProgressIndicator()
                    } else {
                        IconButton(onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }) {
                            Icon(Icons.Default.MyLocation, "Get current location", tint = Saffron)
                        }
                    }
                }
            )

            Text("Urgency", fontWeight = FontWeight.Bold, color = DarkBrown)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Normal", "Urgent", "Immediate").forEach { level ->
                    FilterChip(
                        selected = urgency == level,
                        onClick = { urgency = level },
                        label = { Text(level) }
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red, fontSize = 12.sp)
            }

            if (requestState is RequestState.Error) {
                Text((requestState as RequestState.Error).message, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (foodNeeded.isBlank() || quantity.isBlank() || locationName.isBlank()) {
                        errorMessage = "Please fill all fields"
                    } else {
                        viewModel.createRequest(foodNeeded, quantity, locationName, urgency)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                enabled = requestState !is RequestState.Loading
            ) {
                if (requestState is RequestState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
