package com.example.annapurna.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class FoodRequest(
    val id: String = "",
    
    @SerialName("user_id")
    val userId: String = "",
    
    @SerialName("food_name")
    val foodName: String = "",
    
    val quantity: String = "",
    
    val location: String = "",
    
    val urgency: String = "Normal",
    
    val status: String = "open", // open, fulfilled
    
    @SerialName("matched_food_id")
    val matchedFoodId: String? = null, // ✅ NEW: Link to the food that fulfilled this
    
    @SerialName("assigned_ngo_id")
    val assignedNgoId: String? = null, // ✅ NEW: NGO that matched this
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
