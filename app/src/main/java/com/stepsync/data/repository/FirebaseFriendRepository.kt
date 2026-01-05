package com.stepsync.data.repository

import android.util.Log
import com.google. firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.stepsync.data.model.Friend
import com.stepsync. domain.repository.FriendRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject. Inject

/**
 * Firebase implementation of FriendRepository
 */
class FirebaseFriendRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FriendRepository {

    private val friendsCollection = firestore.collection("friends")
    private val usersCollection = firestore.collection("users")

    override fun getAllFriends(userId: String): Flow<List<Friend>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        Log.d("FriendRepository", "🔵 Listening for friends of user:  ${currentUser.uid}")
        val registration = friendsCollection
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("status", "accepted")

            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FriendRepository", "Error getting friends", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val friends = snapshot?.documents?.mapNotNull { document ->
                    document. toObject(Friend::class.java)?. copy(
                        id = document.id. hashCode().toLong(),
                        userId = currentUser.uid
                    )
                } ?:  emptyList()

                trySend(friends)
            }

        awaitClose { registration.remove() }
    }

    override fun getPendingRequests(userId: String): Flow<List<Friend>> = callbackFlow {
        val currentUser = auth. currentUser
        if (currentUser == null) {

            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        Log.d("FriendRepository", "🔵 Listening for pending requests for user: ${currentUser.uid}")
        val registration = friendsCollection
            .whereEqualTo("friendUserId", currentUser.uid)
            .whereEqualTo("status", "pending")

            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FriendRepository", "Error getting pending requests", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val friends = snapshot?.documents?.mapNotNull { document ->

                    document. toObject(Friend::class.java)?.copy(
                        id = document.id.hashCode().toLong()
                    )
                } ?: emptyList()

                trySend(friends)
            }

        awaitClose { registration. remove() }
    }

    override suspend fun addFriendByCode(userId: String, friendCode: String) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            Log.d("FriendRepository", "Looking for user with friend code: $friendCode")

            // Find user by friend code
            val userQuery = usersCollection
                .whereEqualTo("friendCode", friendCode)
                .limit(1)
                .get()
                .await()

            if (userQuery.isEmpty) {
                Log.e("FriendRepository", "User not found with friend code: $friendCode")
                throw Exception("User not found with friend code: $friendCode")
            }

            val friendDoc = userQuery.documents[0]
            val friendUserId = friendDoc. id
            val friendName = friendDoc.getString("name") ?: "Unknown"
            val friendEmail = friendDoc.getString("email") ?: ""

            Log.d("FriendRepository", "Found friend: $friendName (ID: $friendUserId)")

            // Check if trying to add yourself
            if (friendUserId == currentUser.uid) {
                Log.e("FriendRepository", "❌ User trying to add themselves")
                throw Exception("You cannot add yourself as a friend")
            }

            // Check if already friends or request exists
            Log.d("FriendRepository", "🔵 Checking for existing friend relationship...")
            val existingFriendQuery = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("friendUserId", friendUserId)
                .limit(1)
                .get()
                .await()

            if (!existingFriendQuery.isEmpty) {
                val status = existingFriendQuery.documents[0].getString("status")
                Log.e("FriendRepository", "❌ Friend relationship already exists with status: $status")
                val message = when (status) {
                    "accepted" -> "Already friends with this user"
                    "pending" -> "Friend request already sent"
                    else -> "Friend request already exists"
                }
                throw Exception(message)
            }

            // Check if friend already sent you a request
            Log.d("FriendRepository", "🔵 Checking for reverse friend request...")
            val reverseRequestQuery = friendsCollection
                . whereEqualTo("userId", friendUserId)
                .whereEqualTo("friendUserId", currentUser.uid)
                .limit(1)
                .get()
                .await()

            if (!reverseRequestQuery.isEmpty) {
                Log.e("FriendRepository", "❌ Reverse request already exists")
                throw Exception("This user has already sent you a friend request.  Check your pending requests!")
            }

            // Create friend request
            val friendData = hashMapOf(
                "userId" to currentUser.uid,
                "friendUserId" to friendUserId,
                "friendName" to friendName,
                "friendEmail" to friendEmail,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis()
            )
            Log.d("FriendRepository", "🔵 Creating friend request document...")
            Log.d("FriendRepository", "Friend request data: $friendData")

            friendsCollection.add(friendData).await()
            Log.d("FriendRepository", "Friend request created successfully using friend code")

        } catch (e: Exception) {
            Log.e("FriendRepository", "Failed to add friend by code: ${e.message}", e)
            throw e
        }
    }


    override suspend fun acceptFriendRequest(friendId: Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            Log.d("FriendRepository", "🔵 Accepting friend request: $friendId")

            val querySnapshot = friendsCollection
                . whereEqualTo("friendUserId", currentUser.uid)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            Log.d("FriendRepository", "Found ${querySnapshot.documents.size} pending requests")

            val document = querySnapshot.documents.find {
                val docId = it.id. hashCode().toLong()
                Log.d("FriendRepository", "Comparing document ID hash $docId with friendId $friendId")
                docId == friendId
            } ?: throw Exception("Friend request not found")

            Log.d("FriendRepository", "🔵 Found request document: ${document.id}")

            // Get the original sender's info
            val senderUserId = document.getString("userId") ?: throw Exception("Missing userId")
            val senderName = document.getString("friendName") ?: "Unknown"
            val senderEmail = document.getString("friendEmail") ?: ""

            Log.d("FriendRepository", "Original sender: $senderName ($senderUserId)")
            Log.d("FriendRepository", "Current user (accepter): ${currentUser.uid}")

            // Step 1: Update the original request to "accepted"
            friendsCollection. document(document.id)
                .update("status", "accepted")
                .await()

            Log. d("FriendRepository", "✅ Original request updated to accepted")

            // Step 2: Create the reverse friendship so BOTH users see each other as friends
            // Get current user's info
            val currentUserDoc = usersCollection.document(currentUser. uid).get().await()
            val currentUserName = currentUserDoc.getString("name") ?: "Unknown"
            val currentUserEmail = currentUserDoc.getString("email") ?: ""

            // Check if reverse relationship already exists
            val reverseExists = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("friendUserId", senderUserId)
                .get()
                .await()

            if (reverseExists.isEmpty) {
                // Create reverse friendship
                val reverseFriendData = hashMapOf(
                    "userId" to currentUser.uid,
                    "friendUserId" to senderUserId,
                    "friendName" to senderName,
                    "friendEmail" to senderEmail,
                    "status" to "accepted",
                    "createdAt" to System.currentTimeMillis()
                )

                friendsCollection.add(reverseFriendData).await()
                Log.d("FriendRepository", "✅ Reverse friendship created")
            } else {
                Log.d("FriendRepository", "⚠️ Reverse relationship already exists")
            }

            Log.d("FriendRepository", "✅✅✅ Friend request accepted successfully - Both users are now friends!")

        } catch (e: Exception) {
            Log.e("FriendRepository", "❌ Failed to accept friend request:  ${e.message}", e)
            throw Exception("Failed to accept friend request:  ${e.message}")
        }
    }

    override suspend fun removeFriend(friendId: Long) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            Log.d("FriendRepository", "Removing friend:  $friendId")

            val querySnapshot = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val document = querySnapshot.documents. find {
                it.id. hashCode().toLong() == friendId
            }

            // If not found in userId, check friendUserId (for pending requests)
            val document2 = if (document == null) {
                Log. d("FriendRepository", "Not found in sent requests, checking received...")
                val querySnapshot2 = friendsCollection
                    .whereEqualTo("friendUserId", currentUser.uid)
                    . get()
                    .await()

                querySnapshot2.documents.find {
                    it.id.hashCode().toLong() == friendId
                }
            } else {
                document
            }

            if (document2 == null) {
                Log.e("FriendRepository", "❌ Friend not found with ID: $friendId")
                throw Exception("Friend not found")
            }

            Log.d("FriendRepository", "🔵 Deleting document: ${document2.id}")
            friendsCollection.document(document2.id).delete().await()
            Log.d("FriendRepository", "Friend removed successfully")

        } catch (e: Exception) {
            Log.e("FriendRepository", "❌ Failed to remove friend: ${e.message}", e)
            throw Exception("Failed to remove friend: ${e.message}")
        }
    }

    override suspend fun getFriendsCount(userId: String): Int {
        val currentUser = auth.currentUser ?: return 0

        return try {
            val querySnapshot = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            querySnapshot. size()
        } catch (e: Exception) {
            Log.e("FriendRepository", "Error getting friends count", e)
            0
        }
    }
}