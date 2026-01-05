package com.stepsync.data.model

/**
 * Data model for Friend
 * All properties have default values for Firebase Firestore deserialization
 */
data class Friend(
    val id: Long = 0,
    val userId: String = "",       // Changed from Long to String
    val friendUserId: String = "", // Changed from Long to String
    val friendName: String = "",
    val friendEmail: String = "",
    val status: String = "",
    val createdAt: Long = 0
)