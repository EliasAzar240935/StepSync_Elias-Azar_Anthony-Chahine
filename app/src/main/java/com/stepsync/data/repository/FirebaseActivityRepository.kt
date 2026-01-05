package com.stepsync.data.repository

import com.google.firebase. auth.FirebaseAuth
import com.google.firebase.firestore. FirebaseFirestore
import com. google.firebase.firestore.Query
import com.stepsync. data.model.Activity
import com.stepsync.domain.repository.ActivityRepository
import kotlinx. coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow. callbackFlow
import kotlinx. coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase implementation of ActivityRepository
 */
class FirebaseActivityRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ActivityRepository {

    private val activitiesCollection = firestore.collection("activities")

    override fun getAllActivities(userId: String): Flow<List<Activity>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = activitiesCollection
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val activities = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Activity::class.java)?. copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?: emptyList()

                trySend(activities)
            }

        awaitClose { registration.remove() }
    }

    override fun getActivitiesByType(userId: String, type: String): Flow<List<Activity>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = activitiesCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("activityType", type)
            . orderBy("startTime", Query. Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val activities = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Activity::class.java)?.copy(
                        id = document.id.hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?: emptyList()

                trySend(activities)
            }

        awaitClose { registration.remove() }
    }

    override fun getRecentActivities(userId: String, limit: Int): Flow<List<Activity>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = activitiesCollection
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val activities = snapshot?. documents?.mapNotNull { document ->
                    document.toObject(Activity::class.java)?.copy(
                        id = document. id.hashCode().toLong(),
                        userId = currentUser. uid
                    )
                } ?: emptyList()

                trySend(activities)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getTotalCaloriesBetweenTimes(
        userId: String,
        startTime: Long,
        endTime: Long
    ): Float {
        val currentUser = auth.currentUser ?: return 0f

        return try {
            val querySnapshot = activitiesCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereGreaterThanOrEqualTo("startTime", startTime)
                .whereLessThanOrEqualTo("startTime", endTime)
                . get()
                .await()

            querySnapshot.documents. sumOf { document ->
                (document.getDouble("calories") ?: 0.0)
            }. toFloat()
        } catch (e: Exception) {
            0f
        }
    }

    override suspend fun getTotalDistanceBetweenTimes(
        userId: String,
        startTime: Long,
        endTime: Long
    ): Float {
        val currentUser = auth.currentUser ?: return 0f

        return try {
            val querySnapshot = activitiesCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereGreaterThanOrEqualTo("startTime", startTime)
                .whereLessThanOrEqualTo("startTime", endTime)
                .get()
                .await()

            querySnapshot. documents.sumOf { document ->
                (document.getDouble("distance") ?: 0.0)
            }.toFloat()
        } catch (e: Exception) {
            0f
        }
    }

    override suspend fun insertActivity(activity: Activity): Long {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val activityData = hashMapOf(
                "userId" to currentUser. uid,
                "activityType" to activity.activityType,
                "startTime" to activity.startTime,
                "endTime" to activity. endTime,
                "duration" to activity.duration,
                "distance" to activity.distance,
                "calories" to activity.calories,
                "steps" to activity.steps,
                "notes" to activity.notes
            )

            val documentRef = activitiesCollection.add(activityData).await()
            return documentRef.id.hashCode(). toLong()
        } catch (e: Exception) {
            throw Exception("Failed to insert activity: ${e.message}")
        }
    }

    override suspend fun updateActivity(activity: Activity) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = activitiesCollection
                . whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val document = querySnapshot.documents. find {
                it.id. hashCode().toLong() == activity.id
            } ?: throw Exception("Activity not found")

            val activityData = hashMapOf(
                "userId" to currentUser.uid,
                "activityType" to activity.activityType,
                "startTime" to activity. startTime,
                "endTime" to activity.endTime,
                "duration" to activity.duration,
                "distance" to activity.distance,
                "calories" to activity.calories,
                "steps" to activity.steps,
                "notes" to activity.notes
            )

            activitiesCollection.document(document. id).update(activityData as Map<String, Any>).await()
        } catch (e: Exception) {
            throw Exception("Failed to update activity: ${e. message}")
        }
    }

    override suspend fun deleteActivity(activityId: Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val querySnapshot = activitiesCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val document = querySnapshot. documents.find {
                it.id.hashCode().toLong() == activityId
            } ?: throw Exception("Activity not found")

            activitiesCollection.document(document.id).delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete activity: ${e.message}")
        }
    }
}