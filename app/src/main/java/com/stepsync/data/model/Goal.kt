package com.stepsync.data. model

/**
 * Data model for user fitness goals
 */
data class Goal(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val targetSteps: Int = 0,
    val currentSteps: Int = 0,
    val goalType: GoalType = GoalType.DAILY,
    val startDate: Long = 0,
    val endDate:  Long = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val progress: Float
        get() = if (targetSteps > 0) {
            (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
        } else 0f

    val isActive: Boolean
        get() {
            val now = System.currentTimeMillis()
            return ! isCompleted && now in startDate..endDate
        }

    val daysRemaining: Int
        get() {
            val now = System.currentTimeMillis()
            val diff = endDate - now
            return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        }
}

enum class GoalType {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}