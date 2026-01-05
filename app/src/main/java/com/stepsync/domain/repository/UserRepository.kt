package com.stepsync.domain.repository

import com.stepsync.data.model.User
import kotlinx.coroutines. flow.Flow

/**
 * Repository interface for User operations (Domain layer)
 */
interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun getUserByEmail(email: String): User?
    suspend fun createUser(
        email: String,
        password: String,
        name: String,
        age: Int,
        weight: Float,
        height: Float,
        fitnessGoal: String
    ): String  // Changed from Long to String
    suspend fun updateUser(user: User)
    suspend fun updateDailyStepGoal(userId: String, goal: Int)  // Changed from Long to String
    suspend fun authenticateUser(email: String, password: String): User?
    suspend fun logout()
}