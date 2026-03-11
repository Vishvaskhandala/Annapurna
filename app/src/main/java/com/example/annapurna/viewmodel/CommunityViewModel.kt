package com.example.annapurna.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.annapurna.data.model.CommunityPost
import com.example.annapurna.data.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CommunityPostState {
    object Idle : CommunityPostState()
    object Loading : CommunityPostState()
    object Success : CommunityPostState()
    data class Error(val message: String) : CommunityPostState()
}

class CommunityViewModel : ViewModel() {

    private val repository = CommunityRepository()

    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()

    private val _postState = MutableStateFlow<CommunityPostState>(CommunityPostState.Idle)
    val postState: StateFlow<CommunityPostState> = _postState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.fetchAllPosts().onSuccess {
                _posts.value = it
            }.onFailure {
                // Handle error
            }
            _isRefreshing.value = false
        }
    }

    fun createPost(content: String, postType: String, mealsShared: Int? = null) {
        viewModelScope.launch {
            _postState.value = CommunityPostState.Loading
            repository.createPost(
                content = content,
                postType = postType,
                mealsShared = mealsShared
            ).onSuccess {
                _postState.value = CommunityPostState.Success
                loadPosts()
            }.onFailure {
                _postState.value = CommunityPostState.Error(it.message ?: "Failed to create post")
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId).onSuccess {
                loadPosts()
            }
        }
    }

    fun refreshPosts() {
        loadPosts()
    }

    fun resetPostState() {
        _postState.value = CommunityPostState.Idle
    }
}
