package com.example.annapurna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.annapurna.ui.navigation.NavGraph
import com.example.annapurna.ui.theme.FoodWastageReductionTheme
import com.example.annapurna.viewmodel.AuthViewModel
import com.example.annapurna.viewmodel.FoodViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FoodWastageReductionTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Single NavController for whole app
                    val navController = rememberNavController()

                    // 🔥 Shared ViewModels (IMPORTANT)
                    val authViewModel: AuthViewModel = viewModel()
                    val foodViewModel: FoodViewModel = viewModel()


                    // Navigation graph handles ALL screens
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        foodViewModel = foodViewModel
                    )
                }
            }
        }
    }
}
