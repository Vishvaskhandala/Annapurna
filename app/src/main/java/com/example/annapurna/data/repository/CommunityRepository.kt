package com.example.annapurna.data.repository

import android.util.Log
import com.example.annapurna.data.model.CommunityPost
import com.example.annapurna.data.model.User
import com.example.annapurna.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class CommunityRepository {

    private val client = SupabaseClientProvider.client
    private val auth = client.auth

    companion object {
        private const val TAG = "CommunityRepository"
    }

    suspend fun createPost(
        content: String,
        postType: String,
        imageUrl: String? = null,
        mealsShared: Int? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userSession = auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User not logged in"))

            // Fetch full user data for name and type
            val user = client.from("users")
                .select { filter { eq("user_id", userSession.id) } }
                .decodeSingle<User>()

            val postId = UUID.randomUUID().toString()
            val post = CommunityPost(
                postId = postId,
                userId = user.userId,
                userName = user.name,
                userType = user.userType,
                content = content,
                imageUrl = imageUrl,
                mealsShared = mealsShared,
                postType = postType,
                createdAt = System.currentTimeMillis()
            )

            client.from("community_posts").insert(post)
            Result.success(postId)
        } catch (e: Exception) {
            Log.e(TAG, "createPost failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAllPosts(): Result<List<CommunityPost>> = withContext(Dispatchers.IO) {
        try {
            val list = client.from("community_posts")
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CommunityPost>()
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllPosts failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchMyPosts(): Result<List<CommunityPost>> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User not logged in"))

            val list = client.from("community_posts")
                .select {
                    filter { eq("user_id", user.id) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CommunityPost>()
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "fetchMyPosts failed", e)
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("User not logged in"))

            client.from("community_posts").delete {
                filter {
                    eq("post_id", postId)
                    and { eq("user_id", user.id) }
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "deletePost failed", e)
            Result.failure(e)
        }
    }
}
