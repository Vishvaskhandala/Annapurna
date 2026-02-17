package com.example.annapurna.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.annapurna.ui.navigation.Screen
import com.example.annapurna.viewmodel.AuthState
import com.example.annapurna.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

private val Saffron = Color(0xFFFF6F00)
private val DeepSaffron = Color(0xFFE65100)
private val LightSaffron = Color(0xFFFFB300)

@Composable
fun SplashScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "scale"
    )

    LaunchedEffect(authState) {
        delay(1500)
        when (authState) {
            is AuthState.Authenticated -> {
                val user = currentUser
                if (user?.userType == "donor" || user?.userType == "recipient") {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.UserType.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated, is AuthState.Idle, is AuthState.Error -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(DeepSaffron, Saffron, LightSaffron))),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(320.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)).align(Alignment.TopStart).offset((-100).dp, (-100).dp))
        Box(modifier = Modifier.size(200.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)).align(Alignment.BottomEnd).offset(60.dp, 60.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🪔", fontSize = 88.sp, modifier = Modifier.scale(scale))
            Spacer(modifier = Modifier.height(20.dp))
            Text("Annapurna", fontSize = 46.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("अन्न दान • महा दान", fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f), letterSpacing = 3.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Feeding Hope, Reducing Waste", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(72.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.25f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(500, delayMillis = index * 180), RepeatMode.Reverse),
                        label = "dot$index"
                    )
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White.copy(alpha = dotAlpha)))
                }
            }
        }
    }
}