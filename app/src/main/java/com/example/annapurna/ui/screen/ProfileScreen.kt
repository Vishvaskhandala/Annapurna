package com.example.annapurna.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.annapurna.ui.navigation.Screen
import com.example.annapurna.viewmodel.AuthViewModel

private val Saffron = Color(0xFFFF6F00)
private val DeepSaffron = Color(0xFFE65100)
private val LightSaffron = Color(0xFFFFB300)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isDonor = currentUser?.userType == "donor"

    Box(modifier = Modifier.fillMaxSize().background(WarmCream)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Header
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)
                .background(Brush.verticalGradient(colors = listOf(DeepSaffron, Saffron, LightSaffron)))) {

                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(8.dp).align(Alignment.TopStart)) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                IconButton(onClick = { showLogoutDialog = true }, modifier = Modifier.padding(8.dp).align(Alignment.TopEnd)) {
                    Icon(Icons.Default.ExitToApp, "Logout", tint = Color.White)
                }

                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center) {
                        Text(if (isDonor) "🍱" else "🤲", fontSize = 46.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(currentUser?.name ?: "Sevak", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(currentUser?.email ?: "", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp)) {
                        Text(if (isDonor) "🌾  Anna Daata  •  Food Donor" else "🤝  Seva Grahi  •  NGO / Recipient",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Impact Stats
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("My Impact 🌱", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        ProfileStat("🍱", currentUser?.foodDonated?.toString() ?: "0", "Donated")
                        Box(modifier = Modifier.width(1.dp).height(60.dp).background(Color.Gray.copy(alpha = 0.15f)))
                        ProfileStat("🤲", currentUser?.foodReceived?.toString() ?: "0", "Received")
                        Box(modifier = Modifier.width(1.dp).height(60.dp).background(Color.Gray.copy(alpha = 0.15f)))
                        val total = (currentUser?.foodDonated ?: 0) + (currentUser?.foodReceived ?: 0)
                        ProfileStat("🌟", total.toString(), "Total Acts")
                    }
                }
            }

            // Contact Info
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Contact Info", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
                    Spacer(modifier = Modifier.height(14.dp))
                    InfoRow("📧 Email", currentUser?.email ?: "-")
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow("📱 Phone", currentUser?.phone ?: "-")
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow("👤 Role", if (isDonor) "Food Donor (Anna Daata)" else "Recipient / NGO (Seva Grahi)")
                }
            }

            // Wisdom Quote
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Saffron.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(0.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🪔", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("\"He who feeds a hungry soul, feeds God himself\"",
                        fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.65f), fontWeight = FontWeight.Medium, lineHeight = 19.sp)
                }
            }

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD32F2F).copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.ExitToApp, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Leave Annapurna?", fontWeight = FontWeight.Bold) },
            text = { Text("Your compassion will be missed. Come back soon 🙏") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }) { Text("Sign Out", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Stay", color = Saffron, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProfileStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 22.sp)
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Saffron)
        Text(label, fontSize = 12.sp, color = DarkBrown.copy(alpha = 0.5f))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = DarkBrown.copy(alpha = 0.5f), modifier = Modifier.width(100.dp))
        Text(value, fontSize = 13.sp, color = DarkBrown, fontWeight = FontWeight.Medium)
    }
}