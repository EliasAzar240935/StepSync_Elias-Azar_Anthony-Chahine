package com.stepsync.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase. firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Achievement
import com.stepsync. data.model.AchievementCategory
import com.stepsync.data.model.AchievementTier
import com.stepsync.domain.repository.AchievementRepository
import com.stepsync.util.AchievementDefinitions
import com.stepsync.util.NotificationHelper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Enhanced Firebase implementation of AchievementRepository
 */
class FirebaseAchievementRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) : AchievementRepository {

    private val achievementsCollection = firestore.collection("achievements")
    private val TAG = "AchievementRepo"

    override fun getAllAchievements(userId: String): Flow<List<Achievement>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = achievementsCollection
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching achievements", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val achievements = snapshot?.documents?. mapNotNull { document ->
                    try {
                        Achievement(
                            id = document.id,
                            userId = document. getString("userId") ?: "",
                            achievementType = document.getString("achievementType") ?: "",
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            category = AchievementCategory.valueOf(
                                document.getString("category") ?: "STEPS"
                            ),
                            tier = AchievementTier.valueOf(
                                document.getString("tier") ?: "BRONZE"
                            ),
                            iconName = document. getString("iconName") ?: "",
                            requirement = document.getLong("requirement")?.toInt() ?: 0,
                            currentProgress = document.getLong("currentProgress")?.toInt() ?: 0,
                            isUnlocked = document. getBoolean("isUnlocked") ?: false,
                            unlockedAt = document.getLong("unlockedAt") ?: 0,
                            points = document.getLong("points")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing achievement", e)
                        null
                    }
                } ?: emptyList()

                // Sort:  unlocked first (by unlock time), then locked (by tier/points)
                val sorted = achievements.sortedWith(
                    compareByDescending<Achievement> { it.isUnlocked }
                        .thenByDescending { it.unlockedAt }
                        .thenByDescending { it.points }
                )

                trySend(sorted)
            }

        awaitClose { registration.remove() }
    }

    override fun getAchievementsByCategory(
        userId: String,
        category: AchievementCategory
    ): Flow<List<Achievement>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = achievementsCollection
            . whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("category", category.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val achievements = snapshot?.documents?.mapNotNull { doc ->
                    parseAchievementDocument(doc)
                } ?:  emptyList()

                trySend(achievements. sortedByDescending { it.points })
            }

        awaitClose { registration.remove() }
    }

    override fun getUnlockedAchievements(userId: String): Flow<List<Achievement>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = achievementsCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("isUnlocked", true)
            .orderBy("unlockedAt", Query. Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val achievements = snapshot?.documents?.mapNotNull { doc ->
                    parseAchievementDocument(doc)
                } ?: emptyList()

                trySend(achievements)
            }

        awaitClose { registration.remove() }
    }

    override fun getLockedAchievements(userId: String): Flow<List<Achievement>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = achievementsCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("isUnlocked", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val achievements = snapshot?.documents?.mapNotNull { doc ->
                    parseAchievementDocument(doc)
                } ?: emptyList()

                trySend(achievements. sortedByDescending { it.points })
            }

        awaitClose { registration.remove() }
    }

    override suspend fun unlockAchievement(userId: String, achievementType: String) {
        val currentUser = auth.currentUser ?: return

        try {
            val querySnapshot = achievementsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("achievementType", achievementType)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val doc = querySnapshot.documents[0]
                val isAlreadyUnlocked = doc.getBoolean("isUnlocked") ?: false

                if (!isAlreadyUnlocked) {
                    val title = doc.getString("title") ?: "Achievement Unlocked"
                    val points = doc.getLong("points")?.toInt() ?: 0

                    achievementsCollection. document(doc.id).update(
                        mapOf(
                            "isUnlocked" to true,
                            "unlockedAt" to System.currentTimeMillis(),
                            "currentProgress" to (doc.getLong("requirement")?.toInt() ?: 0)
                        )
                    ).await()

                    Log. d(TAG, "🏆 Achievement unlocked: $title (+$points points)")

                    // Send notification
                    NotificationHelper. sendAchievementNotification(context, title, points)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unlock achievement", e)
        }
    }

    override suspend fun updateAchievementProgress(
        userId: String,
        achievementType: String,
        progress: Int
    ) {
        val currentUser = auth.currentUser ?: return

        try {
            val querySnapshot = achievementsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("achievementType", achievementType)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot. isEmpty) {
                val doc = querySnapshot.documents[0]
                val requirement = doc.getLong("requirement")?.toInt() ?: 0
                val isUnlocked = doc.getBoolean("isUnlocked") ?: false

                if (! isUnlocked) {
                    achievementsCollection.document(doc. id).update(
                        "currentProgress", progress
                    ).await()

                    // Auto-unlock if requirement met
                    if (progress >= requirement) {
                        unlockAchievement(userId, achievementType)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update progress", e)
        }
    }

    override suspend fun getAchievementsCount(userId:  String): Int {
        val currentUser = auth.currentUser ?:  return 0
        return try {
            achievementsCollection
                .whereEqualTo("userId", currentUser. uid)
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getTotalPoints(userId: String): Int {
        val currentUser = auth.currentUser ?: return 0
        return try {
            val snapshot = achievementsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("isUnlocked", true)
                .get()
                .await()

            snapshot.documents.sumOf { it.getLong("points")?.toInt() ?: 0 }
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getUnlockedCount(userId: String): Int {
        val currentUser = auth.currentUser ?: return 0
        return try {
            achievementsCollection
                .whereEqualTo("userId", currentUser. uid)
                .whereEqualTo("isUnlocked", true)
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun checkAndUnlockAchievements(
        userId: String,
        maxDailySteps: Int,
        totalSteps: Int,
        consecutiveDays: Int,
        activitiesCount: Int,
        goalsCompleted: Int,
        friendsCount: Int
    ) {
        try {
            // Single-day step achievements
            checkStepAchievement(userId, "first_steps", maxDailySteps, 100)
            checkStepAchievement(userId, "walker", maxDailySteps, 1000)
            checkStepAchievement(userId, "strider", maxDailySteps, 5000)
            checkStepAchievement(userId, "daily_goal", maxDailySteps, 10000)
            checkStepAchievement(userId, "super_stepper", maxDailySteps, 15000)
            checkStepAchievement(userId, "marathon_walker", maxDailySteps, 20000)
            checkStepAchievement(userId, "ultra_walker", maxDailySteps, 30000)

            // Total cumulative steps
            checkProgressAchievement(userId, "total_10k", totalSteps, 10000)
            checkProgressAchievement(userId, "total_100k", totalSteps, 100000)
            checkProgressAchievement(userId, "total_500k", totalSteps, 500000)
            checkProgressAchievement(userId, "total_1m", totalSteps, 1000000)

            // Streaks
            checkProgressAchievement(userId, "streak_3", consecutiveDays, 3)
            checkProgressAchievement(userId, "streak_7", consecutiveDays, 7)
            checkProgressAchievement(userId, "streak_14", consecutiveDays, 14)
            checkProgressAchievement(userId, "streak_30", consecutiveDays, 30)
            checkProgressAchievement(userId, "streak_100", consecutiveDays, 100)

            // Activities
            checkProgressAchievement(userId, "first_activity", activitiesCount, 1)
            checkProgressAchievement(userId, "activity_10", activitiesCount, 10)
            checkProgressAchievement(userId, "activity_50", activitiesCount, 50)
            checkProgressAchievement(userId, "activity_100", activitiesCount, 100)

            // Goals
            checkProgressAchievement(userId, "goal_complete_1", goalsCompleted, 1)
            checkProgressAchievement(userId, "goal_complete_5", goalsCompleted, 5)
            checkProgressAchievement(userId, "goal_complete_10", goalsCompleted, 10)

            // Social
            checkProgressAchievement(userId, "first_friend", friendsCount, 1)
            checkProgressAchievement(userId, "friend_5", friendsCount, 5)
            checkProgressAchievement(userId, "friend_10", friendsCount, 10)

        } catch (e: Exception) {
            Log.e(TAG, "Error checking achievements", e)
        }
    }

    private suspend fun checkStepAchievement(
        userId: String,
        achievementType: String,
        currentSteps: Int,
        requirement: Int
    ) {
        updateAchievementProgress(userId, achievementType, currentSteps)
        if (currentSteps >= requirement) {
            unlockAchievement(userId, achievementType)
        }
    }

    private suspend fun checkProgressAchievement(
        userId: String,
        achievementType: String,
        currentProgress: Int,
        requirement: Int
    ) {
        updateAchievementProgress(userId, achievementType, currentProgress)
        if (currentProgress >= requirement) {
            unlockAchievement(userId, achievementType)
        }
    }

    override suspend fun initializeAchievementsForUser(userId: String) {
        val currentUser = auth.currentUser ?: return

        try {
            // Check if already initialized
            val existing = achievementsCollection
                . whereEqualTo("userId", currentUser.uid)
                .limit(1)
                .get()
                .await()

            if (existing.isEmpty) {
                Log.d(TAG, "Initializing achievements for user:  $userId")

                val definitions = AchievementDefinitions. getAllDefinitions()
                val batch = firestore.batch()

                definitions. forEach { definition ->
                    val docRef = achievementsCollection. document()
                    val data = hashMapOf(
                        "userId" to currentUser.uid,
                        "achievementType" to definition.achievementType,
                        "title" to definition.title,
                        "description" to definition.description,
                        "category" to definition.category.name,
                        "tier" to definition.tier.name,
                        "iconName" to definition.iconName,
                        "requirement" to definition.requirement,
                        "currentProgress" to 0,
                        "isUnlocked" to false,
                        "unlockedAt" to 0L,
                        "points" to definition.points
                    )
                    batch.set(docRef, data)
                }

                batch.commit().await()
                Log.d(TAG, "✅ Initialized ${definitions.size} achievements")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize achievements", e)
        }
    }

    private fun parseAchievementDocument(doc: com.google.firebase.firestore.DocumentSnapshot): Achievement?  {
        return try {
            Achievement(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                achievementType = doc.getString("achievementType") ?: "",
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                category = AchievementCategory.valueOf(doc.getString("category") ?: "STEPS"),
                tier = AchievementTier.valueOf(doc. getString("tier") ?: "BRONZE"),
                iconName = doc.getString("iconName") ?: "",
                requirement = doc.getLong("requirement")?.toInt() ?: 0,
                currentProgress = doc.getLong("currentProgress")?.toInt() ?: 0,
                isUnlocked = doc.getBoolean("isUnlocked") ?: false,
                unlockedAt = doc.getLong("unlockedAt") ?: 0,
                points = doc.getLong("points")?.toInt() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }
}