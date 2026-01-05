package com.stepsync.domain.repository

import com.stepsync.data. model.Goal
import kotlinx. coroutines.flow.Flow

/**
 * Repository interface for Goal operations (Domain layer)
 */
interface GoalRepository {
    fun getAllGoals(userId: String): Flow<List<Goal>>
    fun getActiveGoals(userId: String): Flow<List<Goal>>
    fun getCompletedGoals(userId: String): Flow<List<Goal>>
    suspend fun createGoal(goal: Goal)
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goalId: String)
    suspend fun updateGoalProgress(goalId: String, currentSteps: Int)
    suspend fun markGoalAsCompleted(goalId: String)

    suspend fun updateGoalProgressForUser(userId: String)
}