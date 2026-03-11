package com.example.annapurna

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.annapurna.ui.navigation.NavGraph
import com.example.annapurna.ui.navigation.Screen
import com.example.annapurna.ui.theme.FoodWastageReductionTheme
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.FoodViewModel

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FoodWastageReductionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val foodViewModel: FoodViewModel = viewModel()

                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        foodViewModel = foodViewModel
                    )
                }
            }
        }
        
        // Handle notification click if app was closed
        intent?.let { handleNotificationIntent(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        val type = intent.getStringExtra("type")
        val foodId = intent.getStringExtra("food_id")
        
        Log.d("MainActivity", "Notification Intent received: type=$type, foodId=$foodId")

        if (type != null && ::navController.isInitialized) {
            when (type) {
                "food_claimed" -> navController.navigate("donations_tab") // Adjust based on your bottom nav routes
                "new_food" -> {
                    // Navigate to Map and potentially zoom to foodId
                    navController.navigate("map_tab")
                }
                "request_fulfilled" -> {
                    if (foodId != null) {
                        navController.navigate(Screen.FoodDetails.createRoute(foodId))
                    }
                }
                "completed" -> navController.navigate("profile_tab")
            }
        }
    }
}
