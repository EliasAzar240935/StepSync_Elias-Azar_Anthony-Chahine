package com.stepsync.domain. repository

import com.stepsync.data.model.Achievement
import com.stepsync.data. model.AchievementCategory
import kotlinx.coroutines.flow. Flow

/**
 * Repository interface for Achievement operations (Domain layer)
 */
interface AchievementRepository {
    // Get achievements
    fun getAllAchievements(userId: String): Flow<List<Achievement>>
    fun getAchievementsByCategory(userId: String, category: AchievementCategory): Flow<List<Achievement>>
    fun getUnlockedAchievements(userId: String): Flow<List<Achievement>>
    fun getLockedAchievements(userId:  String): Flow<List<Achievement>>

    // Unlock achievement
    suspend fun unlockAchievement(userId: String, achievementType: String)

    // Progress tracking
    suspend fun updateAchievementProgress(userId: String, achievementType: String, progress: Int)

    // Statistics
    suspend fun getAchievementsCount(userId: String): Int
    suspend fun getTotalPoints(userId: String): Int
    suspend fun getUnlockedCount(userId: String): Int

    // Check and unlock based on user stats
    suspend fun checkAndUnlockAchievements(
        userId: String,
        maxDailySteps: Int,
        totalSteps: Int,
        consecutiveDays: Int,
        activitiesCount: Int,
        goalsCompleted: Int,
        friendsCount: Int
    )

    // Initialize achievements for new user
    suspend fun initializeAchievementsForUser(userId: String)
}