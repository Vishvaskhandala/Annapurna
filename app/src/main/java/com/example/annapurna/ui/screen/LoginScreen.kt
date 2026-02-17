package com.example.annapurna.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.annapurna.ui.navigation.Screen
import com.example.annapurna.viewmodel.AuthState
import com.example.annapurna.viewmodel.AuthViewModel

private val Saffron = Color(0xFFFF6F00)
private val DeepSaffron = Color(0xFFE65100)
private val LightSaffron = Color(0xFFFFB300)
private val EarthGreen = Color(0xFF33691E)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Warm gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepSaffron, Saffron, LightSaffron),
                        endY = 700f
                    )
                )
        )

        // Decorative circles
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 300.dp, y = 60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 20.dp, y = 200.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { -120 }
            ) {
                Column(
                    modifier = Modifier.padding(top = 72.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🪔", fontSize = 52.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Annapurna",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "अन्न दान • महा दान",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connecting surplus food to those in need",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            // Card
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(900)) + slideInVertically(tween(900)) { 400 }
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmCream)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pill indicator
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Saffron.copy(alpha = 0.3f))
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Welcome Back",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBrown
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your compassion is needed today",
                            fontSize = 13.sp,
                            color = DarkBrown.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, null, tint = Saffron)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            enabled = authState !is AuthState.Loading,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Saffron,
                                focusedLabelColor = Saffron,
                                cursorColor = Saffron,
                                unfocusedBorderColor = Saffron.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, null, tint = Saffron)
                            },
                            trailingIcon = {
                                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(
                                        if (passwordVisible) "Hide" else "Show",
                                        color = Saffron, fontSize = 12.sp
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            enabled = authState !is AuthState.Loading,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Saffron,
                                focusedLabelColor = Saffron,
                                cursorColor = Saffron,
                                unfocusedBorderColor = Saffron.copy(alpha = 0.3f)
                            )
                        )

                        if (authState is AuthState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Login Button
                        Button(
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = authState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Saffron,
                                disabledContainerColor = Saffron.copy(alpha = 0.4f)
                            )
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text("Sign In", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = DarkBrown.copy(alpha = 0.15f))
                            Text("  or  ", color = DarkBrown.copy(alpha = 0.4f), fontSize = 13.sp)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = DarkBrown.copy(alpha = 0.15f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { navController.navigate(Screen.Register.route) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Saffron),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Saffron)
                        ) {
                            Text("Join Annapurna", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Impact stats
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Saffron.copy(alpha = 0.08f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ImpactStat(emoji = "🍱", value = "1000+", label = "Meals Shared")
                            ImpactStat(emoji = "🤝", value = "50+", label = "NGOs")
                            ImpactStat(emoji = "🌱", label = "Zero\nWaste", value = "Goal")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpactStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 22.sp)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Saffron)
        Text(text = label, fontSize = 10.sp, color = DarkBrown.copy(alpha = 0.5f), textAlign = TextAlign.Center)
    }
}