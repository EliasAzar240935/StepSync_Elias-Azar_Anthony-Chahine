package com.stepsync.presentation.challenges

import android.content.SharedPreferences
import androidx.lifecycle. ViewModel
import androidx.lifecycle. viewModelScope
import com.stepsync.data.model.Challenge
import com.stepsync.data.model.ChallengeParticipation
import com.stepsync. data.model.LeaderboardEntry
import com.stepsync.domain.repository.ChallengeRepository
import com.stepsync.util.Constants
import dagger.hilt. android.lifecycle.HiltViewModel
import kotlinx.coroutines. flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Challenges screen
 */
@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val sharedPreferences:  SharedPreferences
) : ViewModel() {

    private val userId = sharedPreferences.getString(Constants.KEY_USER_ID, "") ?: ""

    // All active challenges
    val activeChallenges: StateFlow<List<Challenge>> = challengeRepository
        .getAllActiveChallenges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Challenges user has joined
    val myChallenges: StateFlow<List<Challenge>> = challengeRepository
        .getUserChallenges(userId)
        .stateIn(viewModelScope, SharingStarted. WhileSubscribed(5000), emptyList())

    // UI state for operations
    private val _uiState = MutableStateFlow<ChallengeUiState>(ChallengeUiState. Idle)
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    // Selected challenge for detail view
    private val _selectedChallenge = MutableStateFlow<Challenge?>(null)
    val selectedChallenge: StateFlow<Challenge?> = _selectedChallenge.asStateFlow()

    // User participation in selected challenge
    private val _userParticipation = MutableStateFlow<ChallengeParticipation?>(null)
    val userParticipation: StateFlow<ChallengeParticipation?> = _userParticipation.asStateFlow()

    // Leaderboard for selected challenge
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    // Global leaderboard
    val globalLeaderboard: StateFlow<List<LeaderboardEntry>> = challengeRepository
        .getGlobalLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Join a challenge
     */
    fun joinChallenge(challengeId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ChallengeUiState.Loading
                challengeRepository.joinChallenge(userId, challengeId)
                _uiState.value = ChallengeUiState.Success("Successfully joined challenge!")
            } catch (e: Exception) {
                _uiState.value = ChallengeUiState.Error(e.message ?: "Failed to join challenge")
            }
        }
    }

    /**
     * Leave a challenge
     */
    fun leaveChallenge(challengeId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ChallengeUiState.Loading
                challengeRepository.leaveChallenge(userId, challengeId)
                _uiState.value = ChallengeUiState. Success("Successfully left challenge!")
            } catch (e: Exception) {
                _uiState.value = ChallengeUiState.Error(e.message ?:  "Failed to leave challenge")
            }
        }
    }

    /**
     * Check if user has joined a specific challenge
     */
    suspend fun hasJoinedChallenge(challengeId: String): Boolean {
        return try {
            challengeRepository.hasJoinedChallenge(userId, challengeId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Load challenge details
     */
    fun loadChallengeDetails(challengeId: String) {
        viewModelScope.launch {
            try {
                // Load challenge
                val challenge = challengeRepository.getChallengeById(challengeId)
                _selectedChallenge.value = challenge

                // Load user participation
                val participation = challengeRepository.getUserParticipation(userId, challengeId)
                _userParticipation.value = participation

                // Load leaderboard
                challengeRepository.getChallengeLeaderboard(challengeId)
                    .collect { leaderboardData ->
                        // Mark current user in leaderboard
                        _leaderboard.value = leaderboardData. map { entry ->
                            entry.copy(isCurrentUser = entry.userId == userId)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = ChallengeUiState.Error(e.message ?: "Failed to load challenge details")
            }
        }
    }

    /**
     * Reset UI state
     */
    fun resetUiState() {
        _uiState.value = ChallengeUiState.Idle
    }

    /**
     * Clear selected challenge
     */
    fun clearSelectedChallenge() {
        _selectedChallenge.value = null
        _userParticipation.value = null
        _leaderboard.value = emptyList()
    }
}

/**
 * UI state for challenge operations
 */
sealed class ChallengeUiState {
    object Idle : ChallengeUiState()
    object Loading : ChallengeUiState()
    data class Success(val message: String) : ChallengeUiState()
    data class Error(val message: String) : ChallengeUiState()
}