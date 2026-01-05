package com.stepsync. data.repository

import com.google.firebase.auth.FirebaseAuth
import com. google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Challenge
import com.stepsync. data.model.ChallengeParticipation
import com. stepsync.data.model. LeaderboardEntry
import com.stepsync.domain.repository.ChallengeRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines. CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Firebase implementation of ChallengeRepository
 */
class FirebaseChallengeRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ChallengeRepository {

    private val challengesCollection = firestore.collection("challenges")
    private val participationsCollection = firestore.collection("challenge_participations")
    private val usersCollection = firestore.collection("users")
    private val stepRecordsCollection = firestore.collection("stepRecords")

    override fun getActiveChallenges(): Flow<List<Challenge>> = callbackFlow {
        val listener = firestore.collection("challenges")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val challenges = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Challenge(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            stepGoal = doc.getLong("stepGoal")?.toInt() ?: 0,
                            startDate = doc.getLong("startDate") ?: 0L,
                            endDate = doc.getLong("endDate") ?: 0L,
                            participantCount = doc.getLong("participantCount")?.toInt() ?: 0,
                            isActive = doc.getBoolean("isActive") ?: false,
                            createdBy = doc.getString("createdBy") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(challenges)
            }

        awaitClose { listener.remove() }
    }

    // ADD THIS ALIAS
    override fun getAllActiveChallenges(): Flow<List<Challenge>> = getActiveChallenges()

    override fun getUserChallenges(userId: String): Flow<List<Challenge>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = participationsCollection
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val challengeIds = snapshot?.documents?.mapNotNull { document ->
                    document. getString("challengeId")
                } ?: emptyList()

                if (challengeIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Fetch challenge details
                challengesCollection
                    .whereIn("__name__", challengeIds. take(10)) // Firestore limit
                    .get()
                    .addOnSuccessListener { challengeSnapshot ->
                        val challenges = challengeSnapshot.documents. mapNotNull { document ->
                            document.toObject(Challenge:: class.java)?.copy(
                                id = document.id
                            )
                        }
                        trySend(challenges)
                    }
                    . addOnFailureListener {
                        trySend(emptyList())
                    }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getChallengeById(challengeId: String): Challenge? {
        return try {
            val document = challengesCollection. document(challengeId).get().await()
            document. toObject(Challenge::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun joinChallenge(userId: String, challengeId:  String) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            // Check if already joined
            if (hasJoinedChallenge(currentUser.uid, challengeId)) {
                throw Exception("Already joined this challenge")
            }

            // Create participation record
            val participation = hashMapOf(
                "userId" to currentUser.uid,
                "challengeId" to challengeId,
                "currentSteps" to 0,
                "joinedAt" to System.currentTimeMillis(),
                "lastUpdated" to System.currentTimeMillis(),
                "isCompleted" to false
            )

            participationsCollection.add(participation).await()

            // Update participant count
            val challengeDoc = challengesCollection.document(challengeId).get().await()
            val currentCount = challengeDoc.getLong("participantCount")?. toInt() ?: 0
            challengesCollection. document(challengeId)
                .update("participantCount", currentCount + 1)
                .await()

        } catch (e: Exception) {
            throw Exception("Failed to join challenge:  ${e.message}")
        }
    }

    override suspend fun leaveChallenge(userId:  String, challengeId: String) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            // Find participation record
            val querySnapshot = participationsCollection
                . whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("challengeId", challengeId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                // Delete participation
                querySnapshot.documents[0].reference.delete().await()

                // Update participant count
                val challengeDoc = challengesCollection. document(challengeId).get().await()
                val currentCount = challengeDoc.getLong("participantCount")?.toInt() ?: 0
                if (currentCount > 0) {
                    challengesCollection. document(challengeId)
                        .update("participantCount", currentCount - 1)
                        .await()
                }
            }
        } catch (e:  Exception) {
            throw Exception("Failed to leave challenge: ${e. message}")
        }
    }

    override suspend fun hasJoinedChallenge(userId: String, challengeId: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            val querySnapshot = participationsCollection
                .whereEqualTo("userId", currentUser. uid)
                .whereEqualTo("challengeId", challengeId)
                .get()
                .await()

            !querySnapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserParticipation(
        userId: String,
        challengeId: String
    ): ChallengeParticipation? {
        val currentUser = auth.currentUser ?: return null

        return try {
            val querySnapshot = participationsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("challengeId", challengeId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                document.toObject(ChallengeParticipation::class.java)?.copy(
                    id = document.id
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateChallengeProgress(userId: String, challengeId: String, steps: Int) {
        val currentUser = auth.currentUser ?: return

        try {
            val querySnapshot = participationsCollection
                . whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("challengeId", challengeId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val participationDoc = querySnapshot.documents[0]

                // Get challenge to check goal
                val challenge = getChallengeById(challengeId)
                val isCompleted = challenge?.let { steps >= it.stepGoal } ?: false

                participationDoc.reference.update(
                    mapOf(
                        "currentSteps" to steps,
                        "lastUpdated" to System.currentTimeMillis(),
                        "isCompleted" to isCompleted
                    )
                ).await()
            }
        } catch (e: Exception) {
            // Silent fail - not critical
        }
    }

    override fun getChallengeLeaderboard(challengeId: String): Flow<List<LeaderboardEntry>> = callbackFlow {
        val listener = firestore.collection("challenge_participations")
            .whereEqualTo("challengeId", challengeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // Use coroutine to fetch user names
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val entries = mutableListOf<LeaderboardEntry>()

                    snapshot?.documents?.forEach { doc ->
                        val userId = doc.getString("userId") ?: ""
                        val steps = doc.getLong("currentSteps")?.toInt() ?: 0

                        // Fetch user name from users collection
                        val userName = try {
                            val userDoc = firestore.collection("users")
                                .document(userId)
                                .get()
                                .await()
                            userDoc.getString("name") ?: "User ${userId. take(8)}"
                        } catch (e: Exception) {
                            "User ${userId.take(8)}"
                        }

                        entries.add(
                            LeaderboardEntry(
                                userId = userId,
                                userName = userName,
                                steps = steps,
                                rank = 0,
                                isCurrentUser = false
                            )
                        )
                    }

                    // Sort by steps descending and assign ranks
                    val sortedEntries = entries
                        .sortedByDescending { it.steps }
                        .mapIndexed { index, entry ->
                            entry.copy(rank = index + 1)
                        }

                    trySend(sortedEntries)
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getGlobalLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val currentUser = auth.currentUser

        // Get all users and calculate their total steps
        val registration = usersCollection
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // FIX:  Safely handle nullable snapshot
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val userIds = snapshot. documents.map { it.id }
                if (userIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // For each user, get their total steps
                val leaderboardEntries = mutableListOf<LeaderboardEntry>()
                var processedCount = 0

                userIds.forEach { userId ->
                    stepRecordsCollection
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { stepSnapshot ->
                            val totalSteps = stepSnapshot.documents.sumOf {
                                it.getLong("steps")?.toInt() ?: 0
                            }

                            val userDoc = snapshot.documents.find { it.id == userId }
                            val userName = userDoc?.getString("name") ?: "Unknown"

                            leaderboardEntries.add(
                                LeaderboardEntry(
                                    userId = userId,
                                    userName = userName,
                                    userEmail = "",
                                    steps = totalSteps,
                                    rank = 0, // Will be set after sorting
                                    isCurrentUser = userId == currentUser?. uid
                                )
                            )

                            processedCount++
                            if (processedCount == userIds.size) {
                                // Sort and assign ranks
                                val sortedLeaderboard = leaderboardEntries
                                    .sortedByDescending { it.steps }
                                    . mapIndexed { index, entry ->
                                        entry.copy(rank = index + 1)
                                    }
                                trySend(sortedLeaderboard)
                            }
                        }
                        .addOnFailureListener {
                            processedCount++
                            if (processedCount == userIds.size) {
                                val sortedLeaderboard = leaderboardEntries
                                    .sortedByDescending { it.steps }
                                    .mapIndexed { index, entry ->
                                        entry.copy(rank = index + 1)
                                    }
                                trySend(sortedLeaderboard)
                            }
                        }
                }
            }

        awaitClose { registration.remove() }
    }
}