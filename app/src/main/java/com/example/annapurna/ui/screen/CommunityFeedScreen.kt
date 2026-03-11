package com.example.annapurna.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.annapurna.data.model.CommunityPost
import com.example.annapurna.viewmodel.CommunityViewModel
import com.example.annapurna.viewmodel.CommunityPostState

private val Saffron = Color(0xFFFF6F00)
private val WarmCream = Color(0xFFFFF8E1)
private val DarkBrown = Color(0xFF3E2723)
private val EarthGreen = Color(0xFF33691E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CommunityFeedScreen(viewModel: CommunityViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val postState by viewModel.postState.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshPosts() }
    )

    LaunchedEffect(postState) {
        if (postState is CommunityPostState.Success) {
            showCreateDialog = false
            viewModel.resetPostState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Community Feed", fontWeight = FontWeight.Bold, color = DarkBrown) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = WarmCream,
                    titleContentColor = DarkBrown
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Saffron,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        containerColor = WarmCream
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).pullRefresh(pullRefreshState)) {
            if (posts.isEmpty() && !isRefreshing) {
                EmptyCommunityState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(posts, key = { it.postId }) { post ->
                        CommunityPostCard(post)
                    }
                }
            }
            
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = Saffron
            )
        }

        if (showCreateDialog) {
            CreatePostDialog(
                onDismiss = { showCreateDialog = false },
                onShare = { content, type, meals ->
                    viewModel.createPost(content, type, meals)
                },
                isLoading = postState is CommunityPostState.Loading
            )
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
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Saffron.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when (post.userType.lowercase()) {
                            "donor" -> "🍱"
                            "ngo" -> "🏢"
                            else -> "🙏"
                        },
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = post.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBrown)
                    Text(text = "${post.userType.replaceFirstChar { it.uppercase() }} • ${getPostTimeAgo(post.createdAt)}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Type & Content
            Row(verticalAlignment = Alignment.CenterVertically) {
                PostTypeBadge(type = post.postType)
                if (post.mealsShared != null && post.mealsShared > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = EarthGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "🌾 ${post.mealsShared} meals shared",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EarthGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = post.content, fontSize = 14.sp, color = DarkBrown, lineHeight = 20.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { /* Like */ }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { /* Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun PostTypeBadge(type: String) {
    val color = when (type.lowercase()) {
        "event" -> Color(0xFF1565C0)
        "story" -> Color(0xFF7B1FA2)
        else -> EarthGreen // Impact
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onShare: (String, String, Int?) -> Unit,
    isLoading: Boolean
) {
    var content by remember { mutableStateOf("") }
    var postType by remember { mutableStateOf("impact") }
    var mealsShared by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Your Impact", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("What's on your mind?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = postType.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Post Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("impact", "event", "story").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    postType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (postType == "impact") {
                    OutlinedTextField(
                        value = mealsShared,
                        onValueChange = { if (it.all { char -> char.isDigit() }) mealsShared = it },
                        label = { Text("Meals Shared (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onShare(content, postType, mealsShared.toIntOrNull()) },
                enabled = content.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) {
                if (isLoading) {
                    // Using the indeterminate CircularProgressIndicator from Material 3 correctly
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Share")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EmptyCommunityState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌱", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No community posts yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBrown)
        Text(
            "Be the first to share your impact story or organize an event!",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

fun getPostTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        else -> "$days d ago"
    }
}
