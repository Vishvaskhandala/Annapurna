package com.example.annapurna.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.annapurna.ui.navigation.BottomNavItem
import com.example.annapurna.ui.navigation.Screen
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.FoodViewModel

@Composable
fun DonorMainScreen(
    navController: NavHostController, // Root navigator
    authViewModel: AuthViewModel,
    foodViewModel: FoodViewModel
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Donations,
        BottomNavItem.Map,
        BottomNavItem.Community,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            // STEP 6: Conditional FAB logic
            // Only show 'Add Donation' on Home and Donations tabs
            if (currentRoute == BottomNavItem.Home.route || currentRoute == BottomNavItem.Donations.route) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.PostFood.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Donation")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                DashboardScreen(authViewModel, foodViewModel)
            }
            composable(BottomNavItem.Donations.route) {
                MyDonationsScreen(foodViewModel)
            }
            composable(BottomNavItem.Map.route) {
                MapView(foodViewModel)
            }
            composable(BottomNavItem.Community.route) {
                CommunityFeedScreen()
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(navController, authViewModel)
            }
        }
    }
}
