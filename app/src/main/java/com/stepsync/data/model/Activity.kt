package com.stepsync.data.model

/**
 * Data model for Activity
 * All properties have default values for Firebase Firestore deserialization
 */
data class Activity(
    val id: Long = 0,
    val userId: String = "",
    val activityType: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val duration: Int = 0,
    val distance: Float = 0f,
    val calories: Float = 0f,
    val steps: Int = 0,
    val notes: String = ""
)
