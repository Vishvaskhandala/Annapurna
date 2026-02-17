package com.example.annapurna.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.annapurna.data.model.User
import com.example.annapurna.data.remote.SupabaseClientProvider
import com.example.annapurna.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val userId = repository.getCurrentUserId()
                Log.d("AuthViewModel", "checkCurrentUser: userId: $userId")

                if (userId == null) {
                    _authState.value = AuthState.Unauthenticated
                    _currentUser.value = null
                    return@launch
                }

                val result = repository.getUserData(userId)

                result.onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                    saveFcmToken()
                    Log.d("AuthViewModel", "checkCurrentUser: success, user: $user")
                }.onFailure { exception ->
                    Log.e("AuthViewModel", "checkCurrentUser: failed to get user data", exception)
                    repository.logout()
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "checkCurrentUser: exception", e)
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    private fun saveFcmToken() {
        viewModelScope.launch {
            try {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        viewModelScope.launch {
                            val userId = repository.getCurrentUserId() ?: return@launch
                            SupabaseClientProvider.client
                                .from("users")
                                .update({ set("fcm_token", token) }) {
                                    filter { eq("user_id", userId) }
                                }
                            Log.d("FCM", "✅ Token saved for user: $userId")
                        }
                    }
                    .addOnFailureListener {
                        Log.e("FCM", "❌ Failed to get FCM token", it)
                    }
            } catch (e: Exception) {
                Log.e("FCM", "❌ Exception while saving token", e)
            }
        }
    }

    suspend fun updateUserType(userId: String, userType: String): Result<Unit> {
        return repository.updateUserType(userId, userType)
    }

    fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        userType: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "register: email: $email, name: $name")

            val result = repository.register(email, password, name, phone, userType)

            result.onSuccess {
                Log.d("AuthViewModel", "register: success")
                checkCurrentUser()
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                Log.e("AuthViewModel", "register: failed", exception)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "login: email: $email")

            val result = repository.login(email, password)

            result.onSuccess {
                Log.d("AuthViewModel", "login: success")
                checkCurrentUser()
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Login failed")
                Log.e("AuthViewModel", "login: failed", exception)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "logout")

            // ✅ FIX ISSUE 2: Clear UI state FIRST, then call server signOut
            // Old order: repository.logout() → then set state
            // Problem: signOut() could trigger session callbacks that call checkCurrentUser()
            // BEFORE state was Unauthenticated → race condition → auto re-login
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated  // ← MOVED BEFORE repository.logout()

            try {
                repository.logout()
            } catch (e: Exception) {
                // UI already shows Login screen — safe to ignore server error
                Log.e("AuthViewModel", "logout: server signOut failed (ignored)", e)
            }
        }
    }

    fun refreshUser() {
        checkCurrentUser()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}