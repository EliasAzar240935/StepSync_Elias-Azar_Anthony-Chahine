package com.stepsync. presentation.home

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle. ViewModel
import androidx.lifecycle. viewModelScope
import com.stepsync.data.model.StepRecord
import com.stepsync.data.model.User
import com.stepsync.domain.repository.GoalRepository
import com.stepsync.domain.repository.StepRecordRepository
import com.stepsync.domain.repository.UserRepository
import com.stepsync.util.Constants
import com.stepsync. util.DateUtils
import com.stepsync.domain.repository.AchievementRepository
import dagger.hilt. android.lifecycle.HiltViewModel
import kotlinx.coroutines. flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val stepRecordRepository: StepRecordRepository,
    private val goalRepository: GoalRepository,
    private val achievementRepository: AchievementRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val userId = sharedPreferences.getString(Constants. KEY_USER_ID, "") ?: ""

    val currentUser:  StateFlow<User? > = userRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todaySteps: StateFlow<StepRecord?> = stepRecordRepository
        .observeStepRecordByDate(userId, DateUtils.getCurrentDate())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentSteps: StateFlow<List<StepRecord>> = stepRecordRepository
        . getRecentStepRecords(userId, 7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState. Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun recordSteps(steps: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading

                val stepRecord = StepRecord(
                    userId = userId,
                    steps = steps,
                    date = System.currentTimeMillis().toString()
                )

                stepRecordRepository.insertStepRecord(stepRecord)

                // UPDATE GOAL PROGRESS
                try {
                    goalRepository.updateGoalProgressForUser(userId)
                    Log.d("HomeViewModel", "✅ Goal progress updated after recording steps")
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "⚠️ Error updating goal progress", e)
                    // Don't fail the whole operation if goal update fails
                }

                _uiState. value = HomeUiState.Success("Steps recorded successfully!")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error recording steps", e)
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to record steps")
            }
        }
    }

    fun refreshSteps() {
        // Refresh goal progress manually
        viewModelScope.launch {
            try {
                goalRepository.updateGoalProgressForUser(userId)
                Log.d("HomeViewModel", "✅ Manual refresh:  Goal progress updated")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "⚠️ Error refreshing goal progress", e)
            }
        }
    }

    fun resetUiState() {
        _uiState.value = HomeUiState.Idle
    }
}

// UI State for Home Screen
sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val message: String) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}