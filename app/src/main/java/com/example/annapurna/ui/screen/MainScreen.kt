package com.example.annapurna.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.FoodViewModel

@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    foodViewModel: FoodViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    // Dynamic UI shell injection based on userType
    when (currentUser?.userType?.lowercase()) {
        "donor" -> {
            DonorMainScreen(
                navController = navController,
                authViewModel = authViewModel,
                foodViewModel = foodViewModel
            )
        }
        "ngo" -> {
            NGOMainScreen(
                navController = navController,
                authViewModel = authViewModel,
                foodViewModel = foodViewModel
            )
        }
        "receiver", "recipient" -> {
            ReceiverMainScreen(
                navController = navController,
                authViewModel = authViewModel,
                foodViewModel = foodViewModel
            )
        }
        else -> {
            // Loading state while fetching user data or if type is not yet set
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
