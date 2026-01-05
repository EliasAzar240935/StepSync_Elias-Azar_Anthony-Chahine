package com.stepsync.presentation.auth

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.stepsync. data.model.User
import com.stepsync.domain.repository.AchievementRepository
import com.stepsync.domain.repository.AuthRepository
import com.stepsync.domain.repository.UserRepository
import com.stepsync.util.Constants
import dagger.hilt. android.lifecycle.HiltViewModel
import kotlinx.coroutines. flow. MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines. flow.stateIn
import android.util.Log
import kotlinx.coroutines.launch
import javax. inject.Inject

/**
 * ViewModel for authentication (login and registration)
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val sharedPreferences: SharedPreferences,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState. Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Observable authentication state - emits the current Firebase user or null
     */
    val authState: StateFlow<FirebaseUser?> = authRepository.observeAuthState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.getCurrentUser())

    /**
     * Check if user is currently authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return authRepository.isUserAuthenticated()
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password. isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password are required")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val user = userRepository.authenticateUser(email, password)
                if (user != null) {
                    saveUserSession(user)
                    try {
                        achievementRepository. initializeAchievementsForUser(user.id)
                        Log.d("AuthViewModel", "✅ Achievements checked/initialized")
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "⚠️ Failed to initialize achievements", e)
                    }
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        age: Int,
        weight: Float,
        height: Float,
        fitnessGoal: String
    ) {
        // Validation
        when {
            email.isBlank() -> {
                _uiState.value = AuthUiState.Error("Email is required")
                return
            }
            password. isBlank() -> {
                _uiState.value = AuthUiState.Error("Password is required")
                return
            }
            password. length < 6 -> {
                _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
                return
            }
            password != confirmPassword -> {
                _uiState.value = AuthUiState.Error("Passwords do not match")
                return
            }
            name.isBlank() -> {
                _uiState.value = AuthUiState.Error("Name is required")
                return
            }
            age <= 0 -> {
                _uiState.value = AuthUiState. Error("Valid age is required")
                return
            }
            weight <= 0 -> {
                _uiState.value = AuthUiState. Error("Valid weight is required")
                return
            }
            height <= 0 -> {
                _uiState.value = AuthUiState.Error("Valid height is required")
                return
            }
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val userId = userRepository.createUser(
                    email = email,
                    password = password,
                    name = name,
                    age = age,
                    weight = weight,
                    height = height,
                    fitnessGoal = fitnessGoal
                )

                // ✅ ADD THIS: Initialize achievements for new user
                try {
                    Log.d("AuthViewModel", "🏆 Initializing achievements for new user...")
                    achievementRepository.initializeAchievementsForUser(userId)
                    Log.d("AuthViewModel", "✅ Achievements initialized")
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "⚠️ Failed to initialize achievements", e)
                }

                // After successful registration, authenticate to get user data
                val user = userRepository.authenticateUser(email, password)
                if (user != null) {
                    saveUserSession(user)
                    _uiState.value = AuthUiState. Success(user)
                } else {
                    _uiState.value = AuthUiState. Error("Registration completed but login failed.  Please try logging in.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    private fun saveUserSession(user: User) {
        sharedPreferences.edit().apply {
            putString(Constants.KEY_USER_ID, user.id)
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout()
                // Clear session
                sharedPreferences.edit().apply {
                    clear()
                    apply()
                }
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AuthUiState. Error("Logout failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState. Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}