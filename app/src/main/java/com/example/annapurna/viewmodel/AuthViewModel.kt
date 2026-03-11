package com.example.annapurna.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.annapurna.data.model.User
import com.example.annapurna.data.remote.SupabaseClientProvider
import com.example.annapurna.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val sharedPrefs = application.getSharedPreferences("annapurna_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _userRole = MutableStateFlow("")
    val userRole: StateFlow<String> = _userRole

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val userId = repository.getCurrentUserId()
                if (userId == null) {
                    _authState.value = AuthState.Unauthenticated
                    _currentUser.value = null
                    _userRole.value = ""
                    clearStoredUserId()
                    return@launch
                }

                val result = repository.getUserData(userId)
                result.onSuccess { user ->
                    _currentUser.value = user
                    _userRole.value = user.userType
                    _authState.value = AuthState.Authenticated
                    storeUserId(user.userId)
                    saveFcmToken()
                }.onFailure {
                    repository.logout()
                    _currentUser.value = null
                    _userRole.value = ""
                    _authState.value = AuthState.Unauthenticated
                    clearStoredUserId()
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    private fun storeUserId(userId: String) {
        sharedPrefs.edit().putString("current_user_id", userId).apply()
    }

    private fun clearStoredUserId() {
        sharedPrefs.edit().remove("current_user_id").apply()
    }

    private fun saveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            viewModelScope.launch {
                val userId = repository.getCurrentUserId() ?: return@launch
                repository.updateFcmToken(userId, token)
            }
        }
    }

    suspend fun updateUserType(userId: String, userType: String): Result<Unit> {
        return repository.updateUserType(userId, userType)
    }

    fun register(email: String, password: String, name: String, phone: String, userType: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.register(email, password, name, phone, userType).onSuccess {
                checkCurrentUser()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(email, password).onSuccess {
                checkCurrentUser()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val userId = _currentUser.value?.userId
            if (userId != null) {
                repository.updateFcmToken(userId, null)
            }
            repository.logout()
            _currentUser.value = null
            _userRole.value = ""
            _authState.value = AuthState.Unauthenticated
            clearStoredUserId()
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
