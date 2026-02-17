package com.example.annapurna.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Screen.UserType.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepSaffron, Saffron, LightSaffron)
                    )
                )
        )

        // Decorative circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 250.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -100 }
            ) {
                Column(
                    modifier = Modifier.padding(top = 56.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🙏", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Join Annapurna",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Be the reason someone smiles today",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Form Card
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 300 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmCream)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pill
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Saffron.copy(alpha = 0.3f))
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Create Your Account",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBrown
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Every account is a step towards zero hunger",
                            fontSize = 12.sp,
                            color = DarkBrown.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Fields
                        AnnapurnaField(
                            value = name,
                            onValueChange = { name = it; errorMessage = "" },
                            label = "Full Name",
                            icon = { Icon(Icons.Default.Person, null, tint = Saffron) },
                            enabled = authState !is AuthState.Loading
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AnnapurnaField(
                            value = email,
                            onValueChange = { email = it; errorMessage = "" },
                            label = "Email Address",
                            icon = { Icon(Icons.Default.Email, null, tint = Saffron) },
                            keyboardType = KeyboardType.Email,
                            enabled = authState !is AuthState.Loading
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AnnapurnaField(
                            value = phone,
                            onValueChange = { phone = it; errorMessage = "" },
                            label = "Phone Number",
                            icon = { Icon(Icons.Default.Phone, null, tint = Saffron) },
                            keyboardType = KeyboardType.Phone,
                            enabled = authState !is AuthState.Loading
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = "" },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Saffron) },
                            trailingIcon = {
                                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(if (passwordVisible) "Hide" else "Show", color = Saffron, fontSize = 12.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            enabled = authState !is AuthState.Loading,
                            shape = RoundedCornerShape(16.dp),
                            colors = annapurnaFieldColors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = "" },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Saffron) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            enabled = authState !is AuthState.Loading,
                            shape = RoundedCornerShape(16.dp),
                            colors = annapurnaFieldColors(),
                            isError = confirmPassword.isNotEmpty() && confirmPassword != password
                        )

                        // Error messages
                        if (errorMessage.isNotEmpty() || authState is AuthState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (errorMessage.isNotEmpty()) errorMessage
                                    else (authState as? AuthState.Error)?.message ?: "",
                                    color = Color(0xFFD32F2F),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Register Button
                        Button(
                            onClick = {
                                when {
                                    name.isBlank() -> errorMessage = "Please enter your name"
                                    !isValidEmail(email) -> errorMessage = "Please enter a valid email"
                                    phone.isBlank() -> errorMessage = "Please enter your phone number"
                                    password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                                    password != confirmPassword -> errorMessage = "Passwords do not match"
                                    else -> viewModel.register(
                                        email = email.trim(),
                                        password = password,
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        userType = "donor"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = authState !is AuthState.Loading,
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
                                Text("Create Account", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Commitment text
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Saffron.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🌾", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "\"Annadanam is the greatest form of dana\" — Ancient wisdom",
                                    fontSize = 12.sp,
                                    color = DarkBrown.copy(alpha = 0.65f),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        TextButton(
                            onClick = { navController.popBackStack() },
                            enabled = authState !is AuthState.Loading
                        ) {
                            Text(
                                "Already helping? Sign In",
                                color = Saffron,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnapurnaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: @Composable () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = icon,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = annapurnaFieldColors()
    )
}

@Composable
private fun annapurnaFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Saffron,
    focusedLabelColor = Saffron,
    cursorColor = Saffron,
    unfocusedBorderColor = Saffron.copy(alpha = 0.3f)
)

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}