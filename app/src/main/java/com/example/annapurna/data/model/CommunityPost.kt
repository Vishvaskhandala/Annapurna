package com.example.annapurna.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityPost(
    @SerialName("post_id")
    val postId: String = "",

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("user_name")
    val userName: String = "",

    @SerialName("user_type")
    val userType: String = "", // "donor", "ngo", "recipient"

    val content: String = "",

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("meals_shared")
    val mealsShared: Int? = null,

    @SerialName("post_type")
    val postType: String = "impact", // "impact", "event", "story"

    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
