package com.example.annapurna.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class UserDonationStats(
    val name: String = "",
    @SerialName("food_donated")
    val foodDonated: Int = 0
)

@Serializable
data class UserReceivedStats(
    @SerialName("food_received")
    val foodReceived: Int = 0
)