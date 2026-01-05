package com.stepsync.data.model

/**
 * Data model for leaderboard entries
 */
data class LeaderboardEntry(
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val steps: Int = 0,
    val rank: Int = 0,
    val isCurrentUser: Boolean = false
)