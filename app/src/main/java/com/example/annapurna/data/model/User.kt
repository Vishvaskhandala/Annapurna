package com.example.annapurna.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@OptIn(InternalSerializationApi::class)
@Serializable
data class User(
    @SerialName("user_id")
    val userId: String = "",

    val name: String = "",

    val email: String = "",

    val phone: String = "",

    @SerialName("user_type")
    val userType: String = "", // "donor" or "recipient"

    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerialName("profile_image_url")
    val profileImageUrl: String = "",

    @SerialName("food_donated")
    val foodDonated: Int = 0,

    @SerialName("food_received")
    val foodReceived: Int = 0
)