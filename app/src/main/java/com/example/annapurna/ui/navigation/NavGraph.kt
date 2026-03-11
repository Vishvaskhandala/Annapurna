package com.example.annapurna.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.annapurna.ui.screen.*
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.FoodViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    foodViewModel: FoodViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(navController, authViewModel)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }

        composable(Screen.UserType.route) {
            UserTypeScreen(navController, authViewModel)
        }

        // ✅ FIXED: Using role-based dispatcher instead of hardcoded DonorMainScreen
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                foodViewModel = foodViewModel
            )
        }

        // ✅ OUTSIDE NAV (Full screen pages)
        composable(Screen.PostFood.route) {
            PostFoodScreen(navController, foodViewModel)
        }

        // Detailed view for food items
        composable(Screen.FoodDetails.route) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            FoodDetailsScreen(navController, foodViewModel, foodId)
        }

        // Create Request Screen for Receivers
        composable(Screen.CreateRequest.route) {
            RequestScreen(navController)
        }
    }
}
