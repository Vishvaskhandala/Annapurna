package com.example.annapurna.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class FoodPost(
    @SerialName("food_id")
    val foodId: String = "",

    @SerialName("donor_id")
    val donorId: String = "",

    @SerialName("donor_name")
    val donorName: String = "",

    @SerialName("food_name")
    val foodName: String = "",

    val quantity: String = "",

    val description: String = "",

    @SerialName("image_url")
    val imageUrl: String = "",

    @SerialName("pickup_time")
    val pickupTime: String = "",

    val location: String = "",

    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    val status: String = "available", // available, claimed_by_ngo, delivered

    @SerialName("claimed_by")
    val claimedBy: String? = null,

    @SerialName("request_id")
    val requestId: String? = null, // ✅ NEW: Link to the request it fulfilled

    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
