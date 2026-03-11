package com.example.annapurna.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class FoodRequest(
    @SerialName("request_id")
    val requestId: String = "",

    @SerialName("ngo_id")
    val ngoId: String = "",

    @SerialName("ngo_name")
    val ngoName: String = "",

    @SerialName("food_type")  // ✅ CORRECT: food_type, not food_name
    val foodType: String = "",

    val quantity: String = "",

    val urgency: String = "",

    val purpose: String = "",

    val location: String = "",

    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    @SerialName("needed_by")
    val neededBy: String = "",

    val status: String = "open",

    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
