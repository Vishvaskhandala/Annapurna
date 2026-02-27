package com.example.annapurna.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object UserType : Screen("user_type")
    object Main : Screen("main")
    object PostFood : Screen("post_food")
    object CreateRequest : Screen("create_request")
    object FoodDetails : Screen("food_details/{foodId}") {
        fun createRoute(foodId: String) = "food_details/$foodId"
    }
    object Profile : Screen("profile")
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home_tab", "Home", Icons.Default.Home)
    object Donations : BottomNavItem("donations_tab", "Donations", Icons.Default.Inventory)
    object Map : BottomNavItem("map_tab", "Map", Icons.Default.LocationOn)
    object Community : BottomNavItem("community_tab", "Community", Icons.Default.Groups)
    object Profile : BottomNavItem("profile_tab", "Profile", Icons.Default.Person)
}

sealed class ReceiverBottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Discover : ReceiverBottomNavItem("discover_tab", "Discover", Icons.Default.Explore)
    object MyRequests : ReceiverBottomNavItem("requests_tab", "My Requests", Icons.AutoMirrored.Filled.ListAlt)
    object Map : ReceiverBottomNavItem("map_tab", "Map", Icons.Default.LocationOn)
    object Profile : ReceiverBottomNavItem("profile_tab", "Profile", Icons.Default.Person)
}

// ✅ NEW: NGO Specific Tabs
sealed class NGOBottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : NGOBottomNavItem("ngo_dashboard", "Dashboard", Icons.Default.Dashboard)
    object Marketplace : NGOBottomNavItem("marketplace", "Marketplace", Icons.Default.Storefront)
    object Requests : NGOBottomNavItem("receiver_requests", "Requests", Icons.AutoMirrored.Filled.ListAlt)
    object Profile : NGOBottomNavItem("profile_tab", "Profile", Icons.Default.Person)
}
