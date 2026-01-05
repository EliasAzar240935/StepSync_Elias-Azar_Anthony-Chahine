package com.stepsync.domain.repository

import com.stepsync.data.model.Friend
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Friend operations (Domain layer)
 */
interface FriendRepository {
    fun getAllFriends(userId: String): Flow<List<Friend>>
    fun getPendingRequests(userId: String): Flow<List<Friend>>
    suspend fun addFriendByCode(userId: String, friendCode: String)  // Changed from friendEmail to friendUsername
    suspend fun acceptFriendRequest(friendId:  Long)
    suspend fun removeFriend(friendId: Long)
    suspend fun getFriendsCount(userId: String): Int
}