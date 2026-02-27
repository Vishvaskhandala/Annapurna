package com.example.annapurna.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.annapurna.data.model.FoodRequest
import com.example.annapurna.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class RequestState {
    object Idle : RequestState()
    object Loading : RequestState()
    object Success : RequestState()
    data class Error(val message: String) : RequestState()
}

class RequestViewModel : ViewModel() {

    private val client = SupabaseClientProvider.client
    private val auth = client.auth

    private val _requests = MutableStateFlow<List<FoodRequest>>(emptyList())
    val requests: StateFlow<List<FoodRequest>> = _requests.asStateFlow()

    private val _requestState = MutableStateFlow<RequestState>(RequestState.Idle)
    val requestState: StateFlow<RequestState> = _requestState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun createRequest(foodName: String, quantity: String, location: String, urgency: String) {
        viewModelScope.launch {
            _requestState.value = RequestState.Loading
            try {
                val user = auth.currentUserOrNull() ?: throw Exception("User not logged in")
                val request = FoodRequest(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    foodName = foodName,
                    quantity = quantity,
                    location = location,
                    urgency = urgency,
                    status = "open"
                )

                client.from("food_requests").insert(request)
                _requestState.value = RequestState.Success
                getUserRequests() // Refresh list
            } catch (e: Exception) {
                _requestState.value = RequestState.Error(e.message ?: "Failed to create request")
            }
        }
    }

    fun getUserRequests() {
        viewModelScope.launch {
            val user = auth.currentUserOrNull() ?: return@launch
            try {
                val list = client.from("food_requests")
                    .select {
                        filter { eq("userId", user.id) }
                        order("createdAt", Order.DESCENDING)
                    }
                    .decodeList<FoodRequest>()
                _requests.value = list
            } catch (e: Exception) {
                Log.e("RequestViewModel", "Failed to fetch requests", e)
            }
        }
    }

    fun refreshRequests() {
        viewModelScope.launch {
            _isRefreshing.value = true
            getUserRequests()
            _isRefreshing.value = false
        }
    }

    fun resetState() {
        _requestState.value = RequestState.Idle
    }
}
