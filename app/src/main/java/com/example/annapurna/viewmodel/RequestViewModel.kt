package com.example.annapurna.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.annapurna.data.model.FoodRequest
import com.example.annapurna.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RequestRepository(application.applicationContext)

    private val _myRequests = MutableStateFlow<List<FoodRequest>>(emptyList())
    val myRequests: StateFlow<List<FoodRequest>> = _myRequests.asStateFlow()

    private val _allOpenRequests = MutableStateFlow<List<FoodRequest>>(emptyList())
    val allOpenRequests: StateFlow<List<FoodRequest>> = _allOpenRequests.asStateFlow()

    private val _createRequestState = MutableStateFlow<RequestState>(RequestState.Idle)
    val createRequestState: StateFlow<RequestState> = _createRequestState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadMyRequests() {
        viewModelScope.launch {
            val result = repository.fetchMyRequests()
            result.onSuccess { requests ->
                _myRequests.value = requests
            }.onFailure { exception ->
                _myRequests.value = emptyList()
            }
        }
    }

    fun loadAllOpenRequests() {
        viewModelScope.launch {
            val result = repository.fetchAllOpenRequests()
            result.onSuccess { requests ->
                _allOpenRequests.value = requests
            }.onFailure { exception ->
                _allOpenRequests.value = emptyList()
            }
        }
    }

    fun createRequest(
        foodType: String,  // ✅ Changed parameter name
        quantity: String,
        urgency: String,
        purpose: String,
        location: String,
        latitude: Double,
        longitude: Double,
        neededBy: String
    ) {
        viewModelScope.launch {
            _createRequestState.value = RequestState.Loading

            val result = repository.createRequest(
                foodType = foodType,  // ✅ Correct parameter
                quantity = quantity,
                urgency = urgency,
                purpose = purpose,
                location = location,
                latitude = latitude,
                longitude = longitude,
                neededBy = neededBy
            )

            result.onSuccess {
                _createRequestState.value = RequestState.Success
                loadMyRequests() // Refresh list
            }.onFailure { exception ->
                _createRequestState.value = RequestState.Error(exception.message ?: "Failed to create request")
            }
        }
    }

    fun fulfillRequest(requestId: String) {
        viewModelScope.launch {
            repository.fulfillRequest(requestId, "")
            loadMyRequests()
            loadAllOpenRequests()
        }
    }

    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            repository.cancelRequest(requestId)
            loadMyRequests()
        }
    }

    fun deleteRequest(requestId: String) {
        viewModelScope.launch {
            repository.deleteRequest(requestId)
            loadMyRequests()
        }
    }

    fun refreshRequests() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadMyRequests()
            loadAllOpenRequests()
            _isRefreshing.value = false
        }
    }

    fun resetCreateState() {
        _createRequestState.value = RequestState.Idle
    }
}

sealed class RequestState {
    object Idle : RequestState()
    object Loading : RequestState()
    object Success : RequestState()
    data class Error(val message: String) : RequestState()
}