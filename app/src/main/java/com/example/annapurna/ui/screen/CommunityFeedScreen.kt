package com.example.annapurna.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val Saffron = Color(0xFFFF6F00)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)

// Mock Data for MVP
data class CommunityPost(
    val id: String,
    val author: String,
    val authorRole: String,
    val content: String,
    val timeAgo: String,
    val imageUrl: String? = null,
    val type: String // "event", "update", "request"
)

val mockPosts = listOf(
    CommunityPost("1", "Helping Hands NGO", "NGO", "We are organizing a mass feeding event this Sunday at Central Park. Volunteers needed! 🤝", "2h ago", type = "event"),
    CommunityPost("2", "Robin Hood Army", "NGO", "Successfully served 500+ meals today thanks to our amazing donors! 🍛", "5h ago", type = "update"),
    CommunityPost("3", "City Shelter", "Recipients", "In urgent need of dry rations for 20 families. Please reach out if you can help. 🙏", "8h ago", type = "request")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Community Feed", fontWeight = FontWeight.Bold, color = DarkBrown) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmCream)
            )
        },
        containerColor = WarmCream
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(mockPosts) { post ->
                CommunityPostCard(post)
            }
        }
    }
}

@Composable
fun CommunityPostCard(post: CommunityPost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Saffron.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Text(if (post.authorRole == "NGO") "🏢" else "🏠")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = post.author, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBrown)
                    Text(text = "${post.authorRole} • ${post.timeAgo}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Type Badge
            TypeBadge(type = post.type)

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            Text(text = post.content, fontSize = 14.sp, color = DarkBrown, lineHeight = 20.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { /* Like */ }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Gray)
                }
                IconButton(onClick = { /* Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TypeBadge(type: String) {
    val color = when (type) {
        "event" -> Color(0xFF1565C0)
        "request" -> Color(0xFFD32F2F)
        else -> Color(0xFF388E3C)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = type.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}
