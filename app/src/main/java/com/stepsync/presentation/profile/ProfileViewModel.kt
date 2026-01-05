package com.stepsync.presentation.profile

import android.content.SharedPreferences
import android.util.Log
import androidx. lifecycle.ViewModel
import androidx. lifecycle.viewModelScope
import com.stepsync.data.model.User
import com.stepsync.domain.repository.UserRepository
import com.stepsync.domain.repository.AchievementRepository
import com.stepsync. util.Constants
import dagger. hilt.android.lifecycle. HiltViewModel
import kotlinx. coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines. flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User? > = userRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    init {
        checkAuthAndLoadUser()
    }

    private fun checkAuthAndLoadUser() {
        viewModelScope.launch {
            // Get userId from SharedPreferences
            val userId = sharedPreferences.getString(Constants.KEY_USER_ID, null)
            val isLoggedIn = sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false)

            Log.d("ProfileViewModel", "UserId: $userId, IsLoggedIn: $isLoggedIn")

            // Check if user is authenticated
            if (userId.isNullOrBlank() || !isLoggedIn) {
                Log.e("ProfileViewModel", "❌ User not authenticated!")
                _uiState.value = ProfileUiState. NotAuthenticated
                return@launch
            }

            // Observe user data
            currentUser.collect { user ->
                if (user == null) {
                    Log.e("ProfileViewModel", "❌ User is NULL!")
                    _uiState. value = ProfileUiState.Error("User data not found")
                } else {
                    Log. d("ProfileViewModel", "✅ User loaded: ${user.name}, Email: ${user.email}, Friend Code: ${user.friendCode}")
                    _uiState. value = ProfileUiState.Success(user)
                }
            }
        }
    }

    fun logout() {
        viewModelScope. launch {
            userRepository.logout()
            sharedPreferences. edit().clear().apply()
            _uiState.value = ProfileUiState.NotAuthenticated
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    object NotAuthenticated :  ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}