package com.stepsync.domain. repository

import com.stepsync.data.model.Challenge
import com.stepsync.data.model.ChallengeParticipation
import com.stepsync.data.model.LeaderboardEntry
import kotlinx. coroutines.flow.Flow

interface ChallengeRepository {
    // Get all active challenges
    fun getActiveChallenges(): Flow<List<Challenge>>
    fun getAllActiveChallenges(): Flow<List<Challenge>>  // ADD THIS ALIAS

    // Get challenges user has joined
    fun getUserChallenges(userId: String): Flow<List<Challenge>>

    // Get specific challenge
    suspend fun getChallengeById(challengeId: String): Challenge?

    // Join/Leave challenges
    suspend fun joinChallenge(userId: String, challengeId: String)
    suspend fun leaveChallenge(userId:  String, challengeId: String)
    suspend fun hasJoinedChallenge(userId: String, challengeId: String): Boolean

    // Get user's participation in a challenge
    suspend fun getUserParticipation(userId:  String, challengeId: String): ChallengeParticipation?

    // Leaderboards
    fun getChallengeLeaderboard(challengeId: String): Flow<List<LeaderboardEntry>>
    fun getGlobalLeaderboard(): Flow<List<LeaderboardEntry>>

    // Update progress
    suspend fun updateChallengeProgress(userId: String, challengeId: String, steps: Int)
}