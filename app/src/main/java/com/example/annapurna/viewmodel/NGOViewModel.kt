package com.example.annapurna.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.data.model.FoodRequest
import com.example.annapurna.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NGOState {
    object Idle : NGOState()
    object Loading : NGOState()
    object Success : NGOState()
    data class Error(val message: String) : NGOState()
}

class NGOViewModel : ViewModel() {

    private val client = SupabaseClientProvider.client
    private val auth = client.auth

    private val _availableFood = MutableStateFlow<List<FoodPost>>(emptyList())
    val availableFood: StateFlow<List<FoodPost>> = _availableFood.asStateFlow()

    private val _claimedFood = MutableStateFlow<List<FoodPost>>(emptyList())
    val claimedFood: StateFlow<List<FoodPost>> = _claimedFood.asStateFlow()

    private val _openRequests = MutableStateFlow<List<FoodRequest>>(emptyList())
    val openRequests: StateFlow<List<FoodRequest>> = _openRequests.asStateFlow()

    private val _ngoState = MutableStateFlow<NGOState>(NGOState.Idle)
    val ngoState: StateFlow<NGOState> = _ngoState.asStateFlow()

    fun fetchInitialData() {
        getAvailableFoodPosts()
        getClaimedByMe()
        getAllOpenRequests()
    }

    private fun getAvailableFoodPosts() {
        viewModelScope.launch {
            try {
                val list = client.from("food_posts")
                    .select {
                        filter { eq("status", "available") }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<FoodPost>()
                _availableFood.value = list
            } catch (e: Exception) {
                Log.e("NGOViewModel", "Failed to fetch available food", e)
            }
        }
    }

    private fun getClaimedByMe() {
        viewModelScope.launch {
            try {
                val user = auth.currentUserOrNull() ?: return@launch
                val list = client.from("food_posts")
                    .select {
                        filter { 
                            eq("claimed_by", user.id)
                            // Show both claimed and in-transit in inventory
                            this@select.filter {
                                or {
                                    eq("status", "claimed_by_ngo")
                                    eq("status", "in_transit")
                                }
                            }
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<FoodPost>()
                _claimedFood.value = list
            } catch (e: Exception) {
                Log.e("NGOViewModel", "Failed to fetch claimed food", e)
            }
        }
    }

    fun getAllOpenRequests() {
        viewModelScope.launch {
            try {
                val list = client.from("food_requests")
                    .select {
                        filter { eq("status", "open") }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<FoodRequest>()
                _openRequests.value = list
            } catch (e: Exception) {
                Log.e("NGOViewModel", "Failed to fetch requests", e)
            }
        }
    }

    fun claimFoodPost(foodId: String) {
        viewModelScope.launch {
            _ngoState.value = NGOState.Loading
            try {
                val user = auth.currentUserOrNull() ?: throw Exception("Not logged in")
                client.from("food_posts").update({
                    set("status", "claimed_by_ngo")
                    set("claimed_by", user.id)
                }) { filter { eq("food_id", foodId) } }
                
                _ngoState.value = NGOState.Success
                fetchInitialData()
            } catch (e: Exception) {
                _ngoState.value = NGOState.Error(e.message ?: "Failed to claim")
            }
        }
    }

    fun updateFoodStatus(foodId: String, newStatus: String) {
        viewModelScope.launch {
            _ngoState.value = NGOState.Loading
            try {
                client.from("food_posts").update({
                    set("status", newStatus)
                }) { filter { eq("food_id", foodId) } }
                
                _ngoState.value = NGOState.Success
                fetchInitialData()
            } catch (e: Exception) {
                _ngoState.value = NGOState.Error(e.message ?: "Failed to update status")
            }
        }
    }

    fun matchRequestWithFood(requestId: String, foodId: String) {
        viewModelScope.launch {
            _ngoState.value = NGOState.Loading
            try {
                val user = auth.currentUserOrNull() ?: throw Exception("Not logged in")
                
                client.from("food_requests").update({
                    set("status", "fulfilled")
                    set("matched_food_id", foodId)
                    set("assigned_ngo_id", user.id)
                }) { filter { eq("id", requestId) } }

                client.from("food_posts").update({
                    set("status", "delivered")
                    set("request_id", requestId)
                }) { filter { eq("food_id", foodId) } }

                _ngoState.value = NGOState.Success
                fetchInitialData()
            } catch (e: Exception) {
                _ngoState.value = NGOState.Error(e.message ?: "Matching failed")
            }
        }
    }

    fun resetState() { _ngoState.value = NGOState.Idle }
}
