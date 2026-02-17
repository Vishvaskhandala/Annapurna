package com.example.annapurna.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.annapurna.data.model.FoodPost
import com.example.annapurna.data.repository.FoodRepository
import com.example.annapurna.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    // Pass application context to repository
    private val repository = FoodRepository(application.applicationContext)
    private val auth = SupabaseClientProvider.client.auth

    private val _availableFood = MutableStateFlow<List<FoodPost>>(emptyList())
    val availableFood: StateFlow<List<FoodPost>> = _availableFood.asStateFlow()

    private val _myDonations = MutableStateFlow<List<FoodPost>>(emptyList())
    val myDonations: StateFlow<List<FoodPost>> = _myDonations.asStateFlow()

    private val _myClaimedFood = MutableStateFlow<List<FoodPost>>(emptyList())
    val myClaimedFood: StateFlow<List<FoodPost>> = _myClaimedFood.asStateFlow()

    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState.asStateFlow()

    private val _claimState = MutableStateFlow<ClaimState>(ClaimState.Idle)
    val claimState: StateFlow<ClaimState> = _claimState.asStateFlow()

    // Track which food is currently being claimed for loading indicator
    private val _claimingFoodId = MutableStateFlow<String?>(null)
    val claimingFoodId: StateFlow<String?> = _claimingFoodId.asStateFlow()

    // Track which food is being deleted
    private val _deletingFoodId = MutableStateFlow<String?>(null)
    val deletingFoodId: StateFlow<String?> = _deletingFoodId.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredFood: StateFlow<List<FoodPost>> =
        combine(_availableFood, _searchQuery) { foodList, query ->

            if (query.isBlank()) return@combine foodList

            foodList.filter {
                it.foodName.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Check if user is logged in and load data
    fun startListeningIfLoggedIn() {
        viewModelScope.launch {
            val user = auth.currentUserOrNull()
            if (user != null) {
                loadAvailableFood()
                loadMyDonations()
                loadMyClaimedFood()
            }
        }
    }
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Load available food (one-time fetch)
    private fun loadAvailableFood() {
        viewModelScope.launch {
            val result = repository.fetchAvailableFood()
            result.onSuccess { foodList ->
                _availableFood.value = foodList
            }.onFailure { exception ->
                _availableFood.value = emptyList()
                // Log error if needed
            }
        }
    }

    // Load user's donations
    private fun loadMyDonations() {
        viewModelScope.launch {
            val result = repository.fetchMyDonations()
            result.onSuccess { foodList ->
                _myDonations.value = foodList
            }.onFailure { exception ->
                _myDonations.value = emptyList()
            }
        }
    }

    // Load food claimed by user
    private fun loadMyClaimedFood() {
        viewModelScope.launch {
            val result = repository.fetchMyClaimedFood()
            result.onSuccess { foodList ->
                _myClaimedFood.value = foodList
            }.onFailure { exception ->
                _myClaimedFood.value = emptyList()
            }
        }
    }

    // Refresh all data
    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadAvailableFood()
            loadMyDonations()
            loadMyClaimedFood()
            delay(500)
            _isRefreshing.value = false
        }
    }

    // Refresh only available food
    fun refreshAvailableFood() {
        loadAvailableFood()
    }

    // Post new food
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

            // Upload image first if provided
            val imageUrl = if (imageUri != null) {
                val uploadResult = repository.uploadFoodImage(imageUri)
                if (uploadResult.isFailure) {
                    _postState.value = PostState.Error("Failed to upload image: ${uploadResult.exceptionOrNull()?.message}")
                    return@launch
                }
                uploadResult.getOrNull() ?: ""
            } else {
                ""
            }

            // Post food
            val result = repository.postFood(
                foodName = foodName,
                quantity = quantity,
                description = description,
                pickupTime = pickupTime,
                imageUrl = imageUrl,
                location = location,
                latitude = latitude,
                longitude = longitude
            )

            result.onSuccess {
                _postState.value = PostState.Success
                // Refresh donations list
                loadMyDonations()
                // Also refresh available food since new food is now available
                loadAvailableFood()
            }.onFailure { exception ->
                _postState.value = PostState.Error(exception.message ?: "Failed to post food")
            }
        }
    }

    // Claim food with per-item loading state
    fun claimFood(foodId: String) {
        viewModelScope.launch {
            // Set loading state for this specific food item
            _claimingFoodId.value = foodId
            _claimState.value = ClaimState.Loading(foodId)

            val result = repository.claimFood(foodId)

            result.onSuccess {
                _claimState.value = ClaimState.Success(foodId)
                // Refresh both available food and claimed food
                loadAvailableFood()
                loadMyClaimedFood()
                // Also refresh donations in case this was donor viewing their post
                loadMyDonations()
            }.onFailure { exception ->
                _claimState.value = ClaimState.Error(
                    foodId = foodId,
                    message = exception.message ?: "Failed to claim food"
                )
            }

            // Clear claiming food ID after completion
            _claimingFoodId.value = null
        }
    }

    // Delete food post with loading state
    fun deleteFood(foodId: String) {
        viewModelScope.launch {
            _deletingFoodId.value = foodId

            val result = repository.deleteFood(foodId)

            result.onSuccess {
                // Refresh donations list
                loadMyDonations()
                // Also refresh available food if the deleted post was available
                loadAvailableFood()
            }.onFailure { exception ->
                // Handle error - you might want to add a delete error state
            }

            _deletingFoodId.value = null
        }
    }

    // Mark food as completed
    fun markAsCompleted(foodId: String) {
        viewModelScope.launch {
            val result = repository.markAsCompleted(foodId)

            result.onSuccess {
                loadMyDonations()
                loadMyClaimedFood()
            }.onFailure { exception ->
                // Handle error
            }
        }
    }

    // Reset states
    fun resetPostState() {
        _postState.value = PostState.Idle
    }

    fun resetClaimState() {
        _claimState.value = ClaimState.Idle
        _claimingFoodId.value = null
    }

    // Clear all states
    fun clearStates() {
        _postState.value = PostState.Idle
        _claimState.value = ClaimState.Idle
        _claimingFoodId.value = null
        _deletingFoodId.value = null
    }

    // Get food post by ID
    fun getFoodById(foodId: String): FoodPost? {
        return _availableFood.value.find { it.foodId == foodId }
            ?: _myDonations.value.find { it.foodId == foodId }
            ?: _myClaimedFood.value.find { it.foodId == foodId }
    }

    // Check if a specific food is currently being claimed
    fun isClaiming(foodId: String): Boolean {
        return _claimingFoodId.value == foodId
    }

    // Get claim state for a specific food item
    fun getClaimStateForFood(foodId: String): ClaimState {
        return when (val state = _claimState.value) {
            is ClaimState.Loading -> if (state.foodId == foodId) state else ClaimState.Idle
            is ClaimState.Success -> if (state.foodId == foodId) state else ClaimState.Idle
            is ClaimState.Error -> if (state.foodId == foodId) state else ClaimState.Idle
            else -> ClaimState.Idle
        }
    }

    // Cancel claim (useful for error recovery)
    fun cancelClaim(foodId: String) {
        if (_claimingFoodId.value == foodId) {
            _claimingFoodId.value = null
            _claimState.value = ClaimState.Idle
        }
    }

    // Retry failed claim
    fun retryClaim(foodId: String) {
        if (_claimState.value is ClaimState.Error) {
            claimFood(foodId)
        }
    }

    // Search food by name or description
    fun searchFood(query: String) {
        viewModelScope.launch {
            val result = repository.searchFood(query)
            result.onSuccess { foodList ->
                _availableFood.value = foodList
            }.onFailure {
                // Handle error
            }
        }
    }

    // Filter food by location
    fun filterByLocation(location: String) {
        viewModelScope.launch {
            val result = repository.filterByLocation(location)
            result.onSuccess { foodList ->
                _availableFood.value = foodList
            }.onFailure {
                // Handle error
            }
        }
    }

    // Get available food count
    fun getAvailableFoodCount(): Int {
        return _availableFood.value.size
    }

    // Get user's donation count
    fun getMyDonationsCount(): Int {
        return _myDonations.value.size
    }

    // Get user's claimed food count
    fun getMyClaimedCount(): Int {
        return _myClaimedFood.value.size
    }

    // Get total meals saved (claimed + completed)
    fun getTotalMealsSaved(): Int {
        return _myClaimedFood.value.count { it.status == "completed" } +
                _myDonations.value.count { it.status == "completed" }
    }
}

// Post state for posting food
sealed class PostState {
    object Idle : PostState()
    object Loading : PostState()
    object Success : PostState()
    data class Error(val message: String) : PostState()

    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
    fun isSuccess(): Boolean = this is Success
    fun isIdle(): Boolean = this is Idle
}

// Enhanced Claim state with food ID tracking
sealed class ClaimState {
    object Idle : ClaimState()
    data class Loading(val foodId: String) : ClaimState()
    data class Success(val foodId: String) : ClaimState()
    data class Error(val foodId: String, val message: String) : ClaimState()

    fun isLoading() = this is Loading
    fun isSuccess() = this is Success
    fun isError() = this is Error
    fun isIdle() = this is Idle

    // 🔥 renamed to avoid JVM conflict
    fun foodIdOrNull(): String? = when (this) {
        is Loading -> foodId
        is Success -> foodId
        is Error -> foodId
        else -> null
    }

    fun errorMessageOrNull(): String? = when (this) {
        is Error -> message
        else -> null
    }
}
