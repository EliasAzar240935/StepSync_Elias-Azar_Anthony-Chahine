package com.stepsync. data.model

/**
 * Data model for Challenge
 * All properties have default values for Firebase Firestore deserialization
 */
data class Challenge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val stepGoal: Int = 0,
    val startDate: Long = 0,
    val endDate: Long = 0,
    val participantCount: Int = 0,
    val isActive: Boolean = false,
    val createdBy:  String = "admin",
    val imageUrl: String = ""
)