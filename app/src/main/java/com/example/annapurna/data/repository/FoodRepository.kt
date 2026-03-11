package com.example.annapurna.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.annapurna.data.model.*
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
            
            // Notify nearby (stubbed radius/logic for edge function)
            edge.sendNotificationToRecipients(foodName, donorName, location, foodId)
            
            Result.success(foodId)
        } catch (e: Exception) {
            Log.e(TAG, "postFood failed", e)
            Result.failure(e)
        }
    }

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

            // Notify donor
            edge.sendNotificationToDonor(
                donorId = foodPost.donorId,
                recipientName = user.userMetadata?.get("name")?.toString() ?: "Someone",
                foodName = foodPost.foodName,
                foodId = foodId
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsCompleted(
        foodId: String,
        rating: Int? = null,
        feedback: String? = null
    ): Result<Boolean> {
        return try {
            val user = auth.currentUserOrNull()
                ?: return Result.failure(Exception("User not logged in"))

            val food = client.from("food_posts")
                .select { filter { eq("food_id", foodId) } }
                .decodeSingle<FoodPost>()

            client.from("food_posts")
                .update({
                    set("status", "completed")
                    set("completed_at", System.currentTimeMillis())
                    rating?.let { set("recipient_rating", it) }
                    feedback?.let { set("recipient_feedback", it) }
                }) { filter { eq("food_id", foodId) } }

            // Update donor stats
            val donor = client.from("users")
                .select { filter { eq("user_id", food.donorId) } }
                .decodeSingle<User>()
            
            client.from("users")
                .update({ set("food_donated", donor.foodDonated + 1) }) {
                    filter { eq("user_id", food.donorId) }
                }

            // Update claimer stats
            if (food.claimedBy != null) {
                val claimer = client.from("users")
                    .select { filter { eq("user_id", food.claimedBy) } }
                    .decodeSingle<User>()

                client.from("users")
                    .update({ set("food_received", claimer.foodReceived + 1) }) {
                        filter { eq("user_id", food.claimedBy) }
                    }
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "markAsCompleted failed", e)
            Result.failure(e)
        }
    }

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
}
