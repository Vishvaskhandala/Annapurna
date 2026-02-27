package com.example.annapurna.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.annapurna.viewmodel.AuthState
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.R
import com.example.annapurna.ui.navigation.Screen

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_animation)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = true
    )

    LaunchedEffect(progress, authState, currentUser) { // ✅ Re-trigger on auth state changes
        if (progress >= 1f) {
            when (authState) {
                is AuthState.Authenticated -> {
                    val user = currentUser
                    if (user != null && user.userType.isNotBlank()) {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else if (user != null) {
                        // Authenticated but no type selected yet
                        navController.navigate(Screen.UserType.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    // If user is null but state is Authenticated, we wait for user data to load.
                }

                is AuthState.Unauthenticated, is AuthState.Error -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                
                is AuthState.Loading, AuthState.Idle -> {
                    // Do nothing, wait for a definitive state.
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}
