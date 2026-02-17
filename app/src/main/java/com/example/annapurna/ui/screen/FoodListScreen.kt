package com.example.annapurna.ui.screen

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.ui.component.FoodPostCard
import io.ktor.websocket.Frame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListScreen(
    navController: NavController,
    title: String,
    foodPosts: List<FoodPost>,
    currentUserId: String?,
    onClaimFood: (String) -> Unit,
    claimingFoodId: String?,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Frame.Text(title) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = foodPosts,
                key = { it.foodId }
            ) { foodPost ->
                FoodPostCard(
                    foodPost = foodPost,
                    currentUserId = currentUserId,
                    isClaiming = claimingFoodId == foodPost.foodId,
                    onClaimClick = { onClaimFood(foodPost.foodId) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(durationMillis = 300),
                        fadeOutSpec = tween(durationMillis = 300)
                    )
                )
            }
        }
    }
}