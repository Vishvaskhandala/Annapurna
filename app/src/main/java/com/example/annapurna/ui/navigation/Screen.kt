package com.example.annapurna.ui.navigation


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object UserType : Screen("user_type")
    object Home : Screen("home")
    object PostFood : Screen("post_food")
    object FoodDetails : Screen("food_details/{foodId}") {
        fun createRoute(foodId: String) = "food_details/$foodId"
    }
    object Profile : Screen("profile")
    object MyActivity : Screen("my_activity")
}