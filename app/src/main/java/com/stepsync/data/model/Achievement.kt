package com.stepsync.data.model

/**
 * Enhanced Achievement data model with categories, tiers, and progress tracking
 */
data class Achievement(
    val id: String = "",
    val userId: String = "",
    val achievementType:  String = "",
    val title:  String = "",
    val description:  String = "",
    val category: AchievementCategory = AchievementCategory.STEPS,
    val tier: AchievementTier = AchievementTier.BRONZE,
    val iconName: String = "",
    val requirement: Int = 0,
    val currentProgress: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0,
    val points: Int = 0
) {
    val progress: Float
        get() = if (requirement > 0) {
            (currentProgress. toFloat() / requirement).coerceIn(0f, 1f)
        } else 0f

    val progressPercentage: Int
        get() = (progress * 100).toInt()
}

enum class AchievementCategory {
    STEPS,
    ACTIVITIES,
    GOALS,
    SOCIAL,
    STREAKS,
    SPECIAL
}

enum class AchievementTier {
    BRONZE,   // 10 points
    SILVER,   // 25 points
    GOLD,     // 50 points
    PLATINUM, // 100 points
    DIAMOND   // 250 points
}