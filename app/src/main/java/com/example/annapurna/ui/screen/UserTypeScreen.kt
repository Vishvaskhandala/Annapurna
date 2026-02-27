package com.example.annapurna.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.annapurna.ui.navigation.Screen
import com.example.annapurna.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

private val Saffron = Color(0xFFFF6F00)
private val DeepSaffron = Color(0xFFE65100)
private val LightSaffron = Color(0xFFFFB300)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)
private val EarthGreen = Color(0xFF33691E)

@Composable
fun UserTypeScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()
        .background(Brush.verticalGradient(colors = listOf(DeepSaffron, Saffron, LightSaffron), endY = 520f))
    ) {
        Box(modifier = Modifier.size(200.dp).offset((-60).dp, (-60).dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)))
        Box(modifier = Modifier.size(140.dp).offset(300.dp, 80.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)))

        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -100 }) {
                Column(modifier = Modifier.padding(top = 68.dp, bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🌾", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("How will you\nserve today?", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White, textAlign = TextAlign.Center, lineHeight = 38.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Every role in Annapurna makes a difference", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                }
            }

            AnimatedVisibility(visible = visible, enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 400 }) {
                Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmCream)) {
                    Column(modifier = Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(48.dp).height(4.dp).clip(CircleShape).background(Saffron.copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.height(28.dp))

                        RoleCard(emoji = "🍱", badge = "ANNA DAATA", title = "Food Donor",
                            desc = "Share your surplus food\nwith those who need it most",
                            isSelected = selectedType == "donor", selectedColor = Saffron,
                            onClick = { selectedType = "donor" })

                        Spacer(modifier = Modifier.height(16.dp))

                        RoleCard(emoji = "🤲", badge = "SEVA GRAHI", title = "Recipient / NGO",
                            desc = "Receive food for yourself\nor your community / NGO",
                            isSelected = selectedType == "recipient", selectedColor = EarthGreen,
                            onClick = { selectedType = "recipient" })

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(12.dp)) {
                                Text(errorMessage ?: "", color = Color(0xFFD32F2F), fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(Saffron.copy(alpha = 0.07f)).padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            // ✅ REPLACED: "🪔" with "🌿"
                            Text("🌿", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("\"The highest act of charity is to give food\" — Indian wisdom",
                                fontSize = 12.sp, color = DarkBrown.copy(alpha = 0.6f), lineHeight = 17.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (selectedType != null && currentUser != null) {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        val result = authViewModel.updateUserType(currentUser!!.userId, selectedType!!)
                                        result.onSuccess {
                                            authViewModel.refreshUser()
                                            // ✅ CORRECTED: Navigate to Screen.Main.route
                                            navController.navigate(Screen.Main.route) { popUpTo(0) { inclusive = true } }
                                        }.onFailure { errorMessage = it.message ?: "Failed to update" }
                                        isLoading = false
                                    }
                                } else errorMessage = "Please choose your role"
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !isLoading && selectedType != null,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron, disabledContainerColor = Saffron.copy(alpha = 0.4f))
                        ) {
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp)
                            else Text("Begin My Journey 🙏", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleCard(emoji: String, badge: String, title: String, desc: String,
                     isSelected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(
            width = if (isSelected) 2.5.dp else 1.dp,
            color = if (isSelected) selectedColor else Color.Gray.copy(alpha = 0.2f),
            shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) selectedColor.copy(alpha = 0.08f) else Color.White),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(68.dp).clip(RoundedCornerShape(18.dp))
                .background(if (isSelected) selectedColor.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.07f)),
                contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 34.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(badge, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) selectedColor else DarkBrown.copy(alpha = 0.4f), letterSpacing = 1.5.sp)
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                Text(desc, fontSize = 12.sp, color = DarkBrown.copy(alpha = 0.55f), lineHeight = 17.sp)
            }
        }
    }
}
