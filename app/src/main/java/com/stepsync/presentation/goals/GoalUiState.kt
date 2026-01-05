package com.stepsync.presentation.goals

sealed class GoalUiState {
    object Idle : GoalUiState()
    object Loading : GoalUiState()
    data class Success(val message: String) : GoalUiState()
    data class Error(val message: String) : GoalUiState()
}