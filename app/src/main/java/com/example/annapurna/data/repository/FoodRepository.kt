package com.example.annapurna.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.annapurna.data.model.*
import com.example.annapurna.data.remote.EdgeNotificationRepository
import com.example.annapurna.data.remote.ImageUploadHelper
import com.example.annapurna.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class FoodRepository(private val context: Context) {

    private val client = SupabaseClientProvider.client
    private val storage = client.storage
    private val auth = client.auth
    private val edge = EdgeNotificationRepository()

    companion object {
        private const val TAG = "FoodRepository"
    }

    // ================= IMAGE UPLOAD =================
    suspend fun uploadFoodImage(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val imageBytes = ImageUploadHelper.compressAndConvertImage(context, imageUri)
                ?: return@withContext Result.failure(Exception("Failed to read image"))

            val fileName = "${UUID.randomUUID()}.jpg"
            val bucket = storage.from("food-images")

            bucket.upload(fileName, imageBytes)
            val publicUrl = bucket.publicUrl(fileName)

            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================= POST FOOD =================
    suspend fun postFood(
        foodName: String,
        quantity: String,
        description: String,
        pickupTime: String,
        imageUrl: String,
        location: String,
        latitude: Double,
        longitude: Double
    ): Result<String> {
        return try {
            val currentUser = auth.currentUserOrNull()
                ?: return Result.failure(Exception("User not logged in"))

            val userResult = client.from("users")
                .select(Columns.list("name", "food_donated")) {
                    filter { eq("user_id", currentUser.id) }
                }
                .decodeSingleOrNull<UserDonationStats>()

            val donorName = userResult?.name ?: "Anonymous"
            val foodId = UUID.randomUUID().toString()

            val foodPost = FoodPost(
                foodId = foodId,
                donorId = currentUser.id,
                donorName = donorName,
                foodName = foodName,
                quantity = quantity,
                description = description,
                imageUrl = imageUrl,
                pickupTime = pickupTime,
                location = location,
                latitude = latitude,
                longitude = longitude,
                status = "available",
                createdAt = System.currentTimeMillis()
            )

            client.from("food_posts").insert(foodPost)

            // update donor stats
            client.from("users")
                .update({ set("food_donated", (userResult?.foodDonated ?: 0) + 1) }) {
                    filter { eq("user_id", currentUser.id) }
                }

            // 🔔 EDGE NOTIFICATION
            edge.sendNotificationToRecipients(foodName, donorName, location)

            Log.d(TAG, "Food posted & edge notification triggered")

            Result.success(foodId)

        } catch (e: Exception) {
            Log.e(TAG, "postFood failed", e)
            Result.failure(e)
        }
    }

    // ================= FETCH AVAILABLE FOOD =================
    suspend fun fetchAvailableFood(): Result<List<FoodPost>> {
        return try {
            val foodList = client.from("food_posts")
                .select {
                    filter { eq("status", "available") }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FoodPost>()

            Result.success(foodList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================= MY DONATIONS =================
    suspend fun fetchMyDonations(): Result<List<FoodPost>> {
        return try {
            val user = auth.currentUserOrNull()
                ?: return Result.failure(Exception("User not logged in"))

            val list = client.from("food_posts")
                .select {
                    filter { eq("donor_id", user.id) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FoodPost>()

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================= MY CLAIMED FOOD =================
    suspend fun fetchMyClaimedFood(): Result<List<FoodPost>> {
        return try {
            val user = auth.currentUserOrNull()
                ?: return Result.failure(Exception("User not logged in"))

            val list = client.from("food_posts")
                .select {
                    filter { eq("claimed_by", user.id) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FoodPost>()

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================= CLAIM FOOD =================
    suspend fun claimFood(foodId: String): Result<Boolean> {
        return try {
            val user = auth.currentUserOrNull()
                ?: return Result.failure(Exception("User not logged in"))

            val foodPost = client.from("food_posts")
                .select { filter { eq("food_id", foodId) } }
                .decodeSingle<FoodPost>()

            client.from("food_posts")
                .update({
                    set("status", "claimed")
                    set("claimed_by", user.id)
                }) { filter { eq("food_id", foodId) } }

            // 🔔 EDGE notify donor
            edge.sendNotificationToDonor(
                donorId = foodPost.donorId,
                recipientName = "Someone",
                foodName = foodPost.foodName
            )

            Result.success(true)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================= DELETE FOOD =================
    suspend fun deleteFood(foodId: String): Result<Boolean> {
        return try {
            val user = auth.currentUserOrNull()
                ?: return Result.failure(Exception("User not logged in"))

            client.from("food_posts").delete {
                filter {
                    eq("food_id", foodId)
                    and { eq("donor_id", user.id) }
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================= MARK COMPLETED =================
    suspend fun markAsCompleted(foodId: String): Result<Boolean> {
        return try {
            client.from("food_posts")
                .update({ set("status", "completed") }) {
                    filter { eq("food_id", foodId) }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================= SEARCH =================
    suspend fun searchFood(query: String): Result<List<FoodPost>> {
        return try {
            val list = client.from("food_posts")
                .select {
                    filter { ilike("food_name", "%$query%") }
                }
                .decodeList<FoodPost>()

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================= FILTER =================
    suspend fun filterByLocation(location: String): Result<List<FoodPost>> {
        return try {
            val list = client.from("food_posts")
                .select { filter { eq("location", location) } }
                .decodeList<FoodPost>()

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
