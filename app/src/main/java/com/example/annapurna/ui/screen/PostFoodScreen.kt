package com.example.annapurna.ui.screen

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.annapurna.viewmodel.FoodViewModel
import com.example.annapurna.viewmodel.PostState
import java.io.File
import java.util.*

private val Saffron = Color(0xFFFF6F00)
private val DeepSaffron = Color(0xFFE65100)
private val LightSaffron = Color(0xFFFFB300)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFoodScreen(navController: NavController, viewModel: FoodViewModel = viewModel()) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var foodName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pickupTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val postState by viewModel.postState.collectAsState()
    var showImageOptions by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, selectedHour: Int, selectedMinute: Int ->
                val amPm = if (selectedHour < 12) "AM" else "PM"
                val displayHour = if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour
                pickupTime = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm)
            },
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            false
        )
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) selectedImageUri = cameraImageUri
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val imageFile = File.createTempFile("camera_image_", ".jpg", context.cacheDir)
            val newUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
            cameraImageUri = newUri
            cameraLauncher.launch(newUri)
        } else {
            errorMessage = "Camera permission is required to take photos."
        }
    }


    LaunchedEffect(postState) {
        if (postState is PostState.Success) {
            viewModel.resetPostState()
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(WarmCream)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Header
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp)
                    .background(Brush.verticalGradient(colors = listOf(DeepSaffron, Saffron, LightSaffron)))
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🍱", fontSize = 36.sp)
                    Text("Share Food, Spread Joy", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Every meal you share matters", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Image picker card
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    onClick = { showImageOptions = true }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri, contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(56.dp).clip(CircleShape)
                                        .background(Saffron.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Saffron, modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Add Food Photo", fontWeight = FontWeight.SemiBold, color = Saffron, fontSize = 15.sp)
                                Text("Tap to select an image", fontSize = 12.sp, color = DarkBrown.copy(alpha = 0.4f))
                            }
                        }
                    }
                }

                PostField(value = foodName, onValueChange = { foodName = it; errorMessage = "" },
                    label = "Food Name *", placeholder = "e.g., Biryani, Dal Rice, Pizza")
                PostField(value = quantity, onValueChange = { quantity = it; errorMessage = "" },
                    label = "Quantity / Servings *", placeholder = "e.g., 10 servings, 5 kg")
                PostField(value = description, onValueChange = { description = it },
                    label = "Description (Optional)", placeholder = "Special notes, ingredients...",
                    minLines = 3, maxLines = 4)
                PostField(value = location, onValueChange = { location = it; errorMessage = "" },
                    label = "Pickup Location *", placeholder = "e.g., Near City Hall, Sector 5")
                PostField(
                    value = pickupTime, onValueChange = {},
                    label = "Pickup Time *", placeholder = "Tap to select time",
                    readOnly = true, onClick = { timePickerDialog.show() }
                )

                // Freshness reminder
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Saffron.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⏰", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Freshness Matters", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                            Text("Specify accurate pickup times so food reaches people fresh.",
                                fontSize = 12.sp, color = DarkBrown.copy(alpha = 0.55f), lineHeight = 17.sp)
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)) {
                        Text(errorMessage, color = Color(0xFFD32F2F), fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp))
                    }
                }

                if (postState is PostState.Error) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)) {
                        Text((postState as PostState.Error).message, color = Color(0xFFD32F2F),
                            fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                    }
                }

                Button(
                    onClick = {
                        when {
                            foodName.isEmpty() -> errorMessage = "Please enter food name"
                            quantity.isEmpty() -> errorMessage = "Please enter quantity"
                            location.isEmpty() -> errorMessage = "Please enter pickup location"
                            pickupTime.isEmpty() -> errorMessage = "Please enter pickup time"
                            else -> viewModel.postFood(
                                imageUri = selectedImageUri,
                                foodName = foodName.trim(),
                                quantity = quantity.trim(),
                                description = description.trim(),
                                pickupTime = pickupTime.trim(),
                                location = location.trim(),
                                latitude = 0.0, longitude = 0.0
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = postState !is PostState.Loading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Saffron,
                        disabledContainerColor = Saffron.copy(alpha = 0.4f)
                    )
                ) {
                    if (postState is PostState.Loading)
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp)
                    else Text("🌾  Share This Food", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ✅ FIX: Use onClick on ListItem instead of .clickable modifier
        if (showImageOptions) {
            ModalBottomSheet(onDismissRequest = { showImageOptions = false }) {
                ListItem(
                    headlineContent = { Text("Take Photo", fontWeight = FontWeight.Medium) },
                    leadingContent = { Icon(Icons.Default.CameraAlt, null, tint = Saffron) },
                    modifier = Modifier,
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    supportingContent = { Text("Use camera to capture food", fontSize = 12.sp) },
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    trailingContent = null
                )
                // Use a Button-based row instead of clickable ListItem
                TextButton(
                    onClick = {
                        showImageOptions = false
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) -> {
                                val imageFile = File.createTempFile("camera_image_", ".jpg", context.cacheDir)
                                val newUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
                                cameraImageUri = newUri
                                cameraLauncher.launch(newUri)
                            }
                            else -> {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBrown)
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Saffron)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Take Photo", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                }

                TextButton(
                    onClick = {
                        showImageOptions = false
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkBrown)
                ) {
                    Icon(Icons.Default.Image, null, tint = Saffron)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Choose from Gallery", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PostField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minLines: Int = 1,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    if (onClick != null) {
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect {
                if (it is PressInteraction.Release) onClick()
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.35f)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Saffron, focusedLabelColor = Saffron,
            cursorColor = Saffron, unfocusedBorderColor = Saffron.copy(alpha = 0.3f)
        ),
        readOnly = readOnly,
        interactionSource = interactionSource
    )
}
