package com.example.annapurna.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.data.repository.FoodRepository
import com.example.annapurna.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- UI States ---

sealed class PostState {
    object Idle : PostState()
    object Loading : PostState()
    object Success : PostState()
    data class Error(val message: String) : PostState()
}

sealed class ClaimState {
    object Idle : ClaimState()
    data class Loading(val foodId: String) : ClaimState()
    data class Success(val foodId: String) : ClaimState()
    data class Error(val foodId: String, val message: String) : ClaimState()
}

sealed class CompletionState {
    object Idle : CompletionState()
    object Loading : CompletionState()
    object Success : CompletionState()
    data class Error(val message: String) : CompletionState()
}

// --- ViewModel ---

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FoodRepository(application.applicationContext)
    private val auth = SupabaseClientProvider.client.auth

    // --- State Streams ---

    private val _availableFood = MutableStateFlow<List<FoodPost>>(emptyList())
    val availableFood: StateFlow<List<FoodPost>> = _availableFood.asStateFlow()

    private val _myDonations = MutableStateFlow<List<FoodPost>>(emptyList())
    val myDonations: StateFlow<List<FoodPost>> = _myDonations.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState.asStateFlow()

    private val _claimState = MutableStateFlow<ClaimState>(ClaimState.Idle)
    val claimState: StateFlow<ClaimState> = _claimState.asStateFlow()

    private val _completionState = MutableStateFlow<CompletionState>(CompletionState.Idle)
    val completionState: StateFlow<CompletionState> = _completionState.asStateFlow()

    // --- Computed State ---

    val filteredFood: StateFlow<List<FoodPost>> =
        combine(_availableFood, _searchQuery) { foodList, query ->
            if (query.isBlank()) return@combine foodList
            foodList.filter {
                it.foodName.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        startListeningIfLoggedIn()
    }

    // --- Initialization & Lifecycle ---

    fun startListeningIfLoggedIn() {
        viewModelScope.launch {
            auth.sessionStatus.collect { status ->
                if (status is io.github.jan.supabase.gotrue.SessionStatus.Authenticated) {
                    refreshData()
                }
            }
        }
    }

    fun startRealtimeSync() {
        viewModelScope.launch {
            while(true) {
                if (auth.currentUserOrNull() != null) {
                    loadAvailableFood()
                }
                delay(30000)
            }
        }
    }

    // --- Data Actions ---

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadAvailableFood()
            loadMyDonations()
            delay(800)
            _isRefreshing.value = false
        }
    }

    private suspend fun loadAvailableFood() {
        repository.fetchAvailableFood()
            .onSuccess { _availableFood.value = it }
            .onFailure { Log.e("FoodViewModel", "Failed to load available food", it) }
    }

    private suspend fun loadMyDonations() {
        repository.fetchMyDonations()
            .onSuccess { _myDonations.value = it }
            .onFailure { Log.e("FoodViewModel", "Failed to load my donations", it) }
    }

    // --- Actions ---

    fun postFood(
        imageUri: Uri?,
        foodName: String,
        quantity: String,
        description: String,
        pickupTime: String,
        location: String,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            val imageUrl = if (imageUri != null) {
                val uploadResult = repository.uploadFoodImage(imageUri)
                if (uploadResult.isFailure) {
                    _postState.value = PostState.Error("Failed to upload image")
                    return@launch
                }
                uploadResult.getOrNull() ?: ""
            } else ""

            repository.postFood(
                foodName = foodName,
                quantity = quantity,
                description = description,
                pickupTime = pickupTime,
                imageUrl = imageUrl,
                location = location,
                latitude = latitude,
                longitude = longitude
            ).onSuccess {
                _postState.value = PostState.Success
                refreshData()
            }.onFailure {
                _postState.value = PostState.Error(it.message ?: "Failed to post food")
            }
        }
    }

    fun claimFood(foodId: String) {
        viewModelScope.launch {
            _claimState.value = ClaimState.Loading(foodId)
            repository.claimFood(foodId).onSuccess {
                _claimState.value = ClaimState.Success(foodId)
                refreshData()
            }.onFailure {
                _claimState.value = ClaimState.Error(foodId, it.message ?: "Failed to claim food")
            }
        }
    }

    fun markAsCompleted(foodId: String) {
        viewModelScope.launch {
            _completionState.value = CompletionState.Loading
            repository.markAsCompleted(foodId).onSuccess {
                _completionState.value = CompletionState.Success
                refreshData()
            }.onFailure {
                _completionState.value = CompletionState.Error(it.message ?: "Failed to complete donation")
            }
        }
    }

    fun deleteFood(foodId: String) {
        viewModelScope.launch {
            repository.deleteFood(foodId).onSuccess { refreshData() }
        }
    }

    fun resetPostState() { _postState.value = PostState.Idle }
    fun resetClaimState() { _claimState.value = ClaimState.Idle }
    fun resetCompletionState() { _completionState.value = CompletionState.Idle }
}
