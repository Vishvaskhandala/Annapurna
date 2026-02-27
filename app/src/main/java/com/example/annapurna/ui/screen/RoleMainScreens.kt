package com.example.annapurna.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.annapurna.ui.navigation.ReceiverBottomNavItem
import com.example.annapurna.ui.navigation.Screen
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.FoodViewModel
import com.example.annapurna.viewmodel.RequestViewModel

@Composable
fun NGOMainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    foodViewModel: FoodViewModel
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("NGO Main Screen - Implementation Pending 🏢", textAlign = TextAlign.Center)
    }
}

@Composable
fun ReceiverMainScreen(
    navController: NavHostController, // Root navigator
    authViewModel: AuthViewModel,
    foodViewModel: FoodViewModel
) {
    val bottomNavController = rememberNavController()
    val requestViewModel: RequestViewModel = viewModel()
    
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        ReceiverBottomNavItem.Discover,
        ReceiverBottomNavItem.MyRequests,
        ReceiverBottomNavItem.Map,
        ReceiverBottomNavItem.Profile
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
            // Show FAB only on "My Requests" tab
            if (currentRoute == ReceiverBottomNavItem.MyRequests.route) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateRequest.route) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Request")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = ReceiverBottomNavItem.Discover.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ReceiverBottomNavItem.Discover.route) {
                // Discover placeholder (will be food feed)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nearby Food Discover Feed\n(Coming in Step 3)", textAlign = TextAlign.Center)
                }
            }
            composable(ReceiverBottomNavItem.MyRequests.route) {
                MyRequestsScreen(requestViewModel)
            }
            composable(ReceiverBottomNavItem.Map.route) {
                MapView(foodViewModel)
            }
            composable(ReceiverBottomNavItem.Profile.route) {
                ProfileScreen(navController, authViewModel)
            }
        }
    }
}
