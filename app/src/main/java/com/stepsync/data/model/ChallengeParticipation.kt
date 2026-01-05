package com.stepsync.data.model

/**
 * Data model for user participation in a challenge
 */
data class ChallengeParticipation(
    val id: String = "",
    val userId: String = "",
    val challengeId: String = "",
    val currentSteps: Int = 0,
    val joinedAt: Long = 0,
    val lastUpdated: Long = 0,
    val isCompleted: Boolean = false
)