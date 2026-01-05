package com.stepsync.data.repository

import android.util.Log
import com.google. firebase.auth.FirebaseAuth
import com. google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Goal
import com.stepsync. data.model.GoalType
import com.stepsync. domain.repository.GoalRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore. FieldValue
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Firebase implementation of GoalRepository
 */
class FirebaseGoalRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : GoalRepository {

    private val goalsCollection = firestore.collection("goals")

    override fun getAllGoals(userId: String): Flow<List<Goal>> = callbackFlow {
        val currentUser = auth. currentUser
        if (currentUser == null) {
            Log.e("FirebaseGoalRepo", "No authenticated user")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log. d("FirebaseGoalRepo", "Listening to all goals for user:  ${currentUser.uid}")

        val registration = goalsCollection
            .whereEqualTo("userId", currentUser.uid)
            // REMOVE THIS LINE → .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseGoalRepo", "Error fetching all goals", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toGoal()
                    } catch (e: Exception) {
                        Log.e("FirebaseGoalRepo", "Error parsing goal document", e)
                        null
                    }
                } ?: emptyList()

                Log.d("FirebaseGoalRepo", "All goals fetched: ${goals. size}")

                // Sort manually in memory
                val sortedGoals = goals.sortedByDescending { it.createdAt }
                trySend(sortedGoals)
            }

        awaitClose { registration.remove() }
    }

    override fun getActiveGoals(userId: String): Flow<List<Goal>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("FirebaseGoalRepo", "No authenticated user for active goals")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log.d("FirebaseGoalRepo", "Listening to active goals for user: ${currentUser.uid}")

        val registration = goalsCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("isCompleted", false)
            // REMOVE THIS LINE → . orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseGoalRepo", "Error fetching active goals", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toGoal()
                    } catch (e: Exception) {
                        Log.e("FirebaseGoalRepo", "Error parsing active goal", e)
                        null
                    }
                } ?:  emptyList()

                Log.d("FirebaseGoalRepo", "Active goals fetched: ${goals.size}")
                goals.forEach {
                    Log.d("FirebaseGoalRepo", "  - ${it.title} (ID: ${it.id})")
                }

                // Sort manually in memory instead
                val sortedGoals = goals.sortedByDescending { it.createdAt }
                trySend(sortedGoals)
            }

        awaitClose { registration. remove() }
    }

    override fun getCompletedGoals(userId:  String): Flow<List<Goal>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("FirebaseGoalRepo", "No authenticated user for completed goals")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        Log.d("FirebaseGoalRepo", "Listening to completed goals for user: ${currentUser.uid}")

        val registration = goalsCollection
            .whereEqualTo("userId", currentUser. uid)
            .whereEqualTo("isCompleted", true)
            // REMOVE THIS LINE → .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseGoalRepo", "Error fetching completed goals", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toGoal()
                    } catch (e: Exception) {
                        Log.e("FirebaseGoalRepo", "Error parsing completed goal", e)
                        null
                    }
                } ?: emptyList()

                Log.d("FirebaseGoalRepo", "Completed goals fetched: ${goals.size}")

                // Sort manually in memory
                val sortedGoals = goals.sortedByDescending { it.createdAt }
                trySend(sortedGoals)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun createGoal(goal: Goal) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val goalData = hashMapOf(
                "userId" to currentUser.uid,
                "title" to goal.title,
                "description" to goal. description,
                "targetSteps" to goal.targetSteps,
                "currentSteps" to goal.currentSteps,
                "goalType" to goal.goalType.name,
                "startDate" to goal.startDate,
                "endDate" to goal.endDate,
                "isCompleted" to false,
                "createdAt" to System.currentTimeMillis(),
                "completedAt" to null
            )

            val docRef = goalsCollection.add(goalData).await()
            Log.d("FirebaseGoalRepo", "Goal created successfully with ID: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("FirebaseGoalRepo", "Error creating goal", e)
            throw e
        }
    }

    override suspend fun updateGoal(goal:  Goal) {
        try {
            val goalData = hashMapOf(
                "title" to goal.title,
                "description" to goal.description,
                "targetSteps" to goal.targetSteps,
                "currentSteps" to goal. currentSteps,
                "goalType" to goal.goalType. name,
                "startDate" to goal.startDate,
                "endDate" to goal.endDate,
                "isCompleted" to goal.isCompleted,
                "completedAt" to goal.completedAt
            )

            goalsCollection. document(goal.id).update(goalData as Map<String, Any>).await()
            Log.d("FirebaseGoalRepo", "Goal updated successfully:  ${goal.id}")
        } catch (e: Exception) {
            Log.e("FirebaseGoalRepo", "Error updating goal", e)
            throw e
        }
    }

    override suspend fun deleteGoal(goalId:  String) {
        try {
            goalsCollection.document(goalId).delete().await()
            Log.d("FirebaseGoalRepo", "Goal deleted successfully: $goalId")
        } catch (e: Exception) {
            Log.e("FirebaseGoalRepo", "Error deleting goal", e)
            throw e
        }
    }

    override suspend fun updateGoalProgress(goalId:  String, currentSteps: Int) {
        try {
            goalsCollection.document(goalId).update("currentSteps", currentSteps).await()
            Log.d("FirebaseGoalRepo", "Goal progress updated: $goalId -> $currentSteps steps")
        } catch (e:  Exception) {
            Log.e("FirebaseGoalRepo", "Error updating goal progress", e)
            throw e
        }
    }

    override suspend fun markGoalAsCompleted(goalId:  String) {
        try {
            val updates = hashMapOf<String, Any>(
                "isCompleted" to true,
                "completedAt" to System. currentTimeMillis()
            )
            goalsCollection.document(goalId).update(updates).await()
            Log.d("FirebaseGoalRepo", "Goal marked as completed: $goalId")
        } catch (e: Exception) {
            Log.e("FirebaseGoalRepo", "Error marking goal as completed", e)
            throw e
        }
    }

    override suspend fun updateGoalProgressForUser(userId: String) {
        try {
            Log.d("FirebaseGoalRepo", "🔄 Updating goal progress for user: $userId")

            // Fetch all active goals for this user
            val activeGoals = goalsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", false)
                .get()
                .await()

            Log.d("FirebaseGoalRepo", "Found ${activeGoals.documents.size} active goals to update")

            // Fetch step records collection
            val stepsCollection = firestore.collection("step_records")

            for (goalDoc in activeGoals.documents) {
                try {
                    val goal = goalDoc.toGoal()
                    Log.d("FirebaseGoalRepo", "Calculating progress for goal: ${goal.title}")

                    // Calculate date range based on goal type
                    val (startDate, endDate) = when (goal.goalType) {
                        GoalType.DAILY -> {
                            val start = getTodayStart()
                            val end = getTodayEnd()
                            Pair(start, end)
                        }
                        GoalType.WEEKLY -> {
                            val start = getWeekStart()
                            val end = System.currentTimeMillis()
                            Pair(start, end)
                        }
                        GoalType.MONTHLY -> {
                            val start = getMonthStart()
                            val end = System.currentTimeMillis()
                            Pair(start, end)
                        }
                        GoalType.CUSTOM -> {
                            Pair(goal.startDate, goal.endDate)
                        }
                    }

                    // Fetch steps in this date range
                    val stepRecords = stepsCollection
                        .whereEqualTo("userId", userId)
                        .whereGreaterThanOrEqualTo("date", startDate)
                        . whereLessThanOrEqualTo("date", endDate)
                        .get()
                        .await()

                    // Sum up total steps
                    val totalSteps = stepRecords. documents.sumOf { doc ->
                        doc.getLong("steps")?.toInt() ?: 0
                    }

                    Log.d("FirebaseGoalRepo", "Goal '${goal.title}':  $totalSteps steps (target: ${goal.targetSteps})")

                    // Update the goal's current steps
                    goalsCollection. document(goal.id).update("currentSteps", totalSteps).await()

                    // Auto-complete if target reached
                    if (totalSteps >= goal.targetSteps && !goal.isCompleted) {
                        Log.d("FirebaseGoalRepo", "🎉 Goal '${goal.title}' completed automatically!")
                        markGoalAsCompleted(goal. id)
                    }

                } catch (e: Exception) {
                    Log.e("FirebaseGoalRepo", "Error updating goal ${goalDoc.id}", e)
                }
            }

            Log.d("FirebaseGoalRepo", "✅ Goal progress update complete")

        } catch (e: Exception) {
            Log.e("FirebaseGoalRepo", "Error updating goal progress", e)
            throw e
        }
    }

    // Helper functions for date calculations
    private fun getTodayStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getTodayEnd(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar. HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar. MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun getWeekStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar. MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getMonthStart(): Long {
        val calendar = Calendar. getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Helper extension function to convert Firestore document to Goal
    private fun com.google.firebase.firestore.DocumentSnapshot.toGoal(): Goal {
        return Goal(
            id = this.id,
            userId = getString("userId") ?: "",
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            targetSteps = getLong("targetSteps")?.toInt() ?: 0,
            currentSteps = getLong("currentSteps")?.toInt() ?: 0,
            goalType = GoalType.valueOf(getString("goalType") ?: "DAILY"),
            startDate = getLong("startDate") ?: 0L,
            endDate = getLong("endDate") ?: 0L,
            isCompleted = getBoolean("isCompleted") ?: false,
            createdAt = getLong("createdAt") ?: 0L,
            completedAt = getLong("completedAt")
        )
    }
}