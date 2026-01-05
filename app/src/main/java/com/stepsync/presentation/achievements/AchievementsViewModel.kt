package com.stepsync.presentation.achievements

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com. stepsync.data.model. Achievement
import com.stepsync.domain.repository.AchievementRepository
import com.stepsync.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val sharedPreferences:  SharedPreferences
) : ViewModel() {

    private val userId = sharedPreferences.getString(Constants.KEY_USER_ID, "") ?: ""

    private val _totalPoints = MutableStateFlow(0)
    val totalPoints: StateFlow<Int> = _totalPoints.asStateFlow()

    private val _unlockedCount = MutableStateFlow(0)
    val unlockedCount: StateFlow<Int> = _unlockedCount.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    // Get ALL achievements (locked and unlocked)
    val achievements: StateFlow<List<Achievement>> = achievementRepository
        .getAllAchievements(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                _totalPoints.value = achievementRepository.getTotalPoints(userId)
                _unlockedCount.value = achievementRepository.getUnlockedCount(userId)
                _totalCount.value = achievementRepository.getAchievementsCount(userId)

                Log.d("AchievementsVM", "Stats loaded: Points=${_totalPoints.value}, Unlocked=${_unlockedCount. value}/${_totalCount.value}")
            } catch (e: Exception) {
                Log.e("AchievementsVM", "Error loading stats", e)
            }
        }
    }
}