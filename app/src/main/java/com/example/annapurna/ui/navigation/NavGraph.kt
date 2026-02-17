package com.example.annapurna.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.annapurna.ui.screen.HomeScreen
import com.example.annapurna.ui.screen.LoginScreen
import com.example.annapurna.ui.screen.MyActivityScreen
import com.example.annapurna.ui.screen.PostFoodScreen
import com.example.annapurna.ui.screen.ProfileScreen
import com.example.annapurna.ui.screen.RegisterScreen
import com.example.annapurna.ui.screen.SplashScreen
import com.example.annapurna.ui.screen.UserTypeScreen
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.FoodViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    foodViewModel: FoodViewModel
) {
    // Create shared FoodViewModel
    val foodViewModel: FoodViewModel = viewModel()

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
        composable(Screen.Home.route) {
            HomeScreen(navController, authViewModel, foodViewModel)
        }
        composable(Screen.PostFood.route) {
            PostFoodScreen(navController, foodViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController, authViewModel)
        }
        composable(Screen.MyActivity.route) {
            MyActivityScreen(navController)
        }
    }
}
