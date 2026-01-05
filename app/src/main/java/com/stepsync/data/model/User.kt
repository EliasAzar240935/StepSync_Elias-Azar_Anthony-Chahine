package com.stepsync.data.model

/**
 * Data model for User
 * All properties must have default values for Firebase Firestore deserialization
 */
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val friendCode: String = "",
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Float = 0f,
    val fitnessGoal: String = "",
    val dailyStepGoal: Int = 10000
)
