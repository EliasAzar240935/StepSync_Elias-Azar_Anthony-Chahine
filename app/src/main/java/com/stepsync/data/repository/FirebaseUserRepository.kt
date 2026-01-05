package com.stepsync.data.repository

import com. google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore. FirebaseFirestore
import com. stepsync.data.model. User
import com.stepsync.domain.repository.UserRepository
import com.stepsync.util.FriendCodeGenerator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase implementation of UserRepository
 */
class FirebaseUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore. collection("users")

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Listen to user document changes
                val docRef = usersCollection.document(firebaseUser.uid)
                docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(User:: class.java)?.copy(
                            id = firebaseUser. uid
                        )
                        trySend(user)
                    } else {
                        trySend(null)
                    }
                }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(authStateListener)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (! querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                document.toObject(User::class. java)?.copy(
                    id = document.id
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createUser(
        email: String,
        password: String,
        name: String,
        age: Int,
        weight: Float,
        height: Float,
        fitnessGoal: String
    ): String {
        try {
            // Create Firebase Auth user
            android.util.Log.d("FirebaseUserRepository", "🔵 Creating Firebase Auth user...")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult. user ?: throw Exception("User creation failed")
            android.util.Log.d("FirebaseUserRepository", "✅ Firebase Auth user created with UID: ${firebaseUser.uid}")
            // Generate unique friend code
            android.util.Log.d("FirebaseUserRepository", "🔵 Generating friend code...")
            val friendCode = generateUniqueFriendCode()
            android.util.Log. d("FirebaseUserRepository", "✅ Friend code generated: $friendCode")

            // Update profile with name
            android.util.Log. d("FirebaseUserRepository", "🔵 Updating Firebase Auth profile...")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser. updateProfile(profileUpdates).await()
            android.util.Log.d("FirebaseUserRepository", "✅ Profile updated")

            // Create user document in Firestore
            val userDoc = hashMapOf(
                "email" to email,
                "name" to name,
                "friendCode" to friendCode,
                "age" to age,
                "weight" to weight,
                "height" to height,
                "fitnessGoal" to fitnessGoal,
                "dailyStepGoal" to 10000,
                "createdAt" to System. currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            android.util.Log. d("FirebaseUserRepository", "🔵 Saving user document to Firestore...")
            android.util.Log.d("FirebaseUserRepository", "User doc data: $userDoc")

            usersCollection.document(firebaseUser.uid).set(userDoc).await()

            android.util.Log. d("FirebaseUserRepository", "✅✅✅ User document saved successfully to Firestore!")
            android.util.Log.d("FirebaseUserRepository", "✅ User created with friend code: $friendCode")
            return firebaseUser. uid
        } catch (e:  FirebaseAuthException) {
            // Handle specific Firebase Auth errors
            val errorMessage = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
                "ERROR_WEAK_PASSWORD" -> "Password is too weak.  Use at least 6 characters"
                "ERROR_INVALID_EMAIL" -> "Invalid email format"
                else -> "Registration failed:  ${e.message}"
            }
            throw Exception(errorMessage)
        }catch (e: com.google.firebase.FirebaseException) {
            android.util.Log.e("FirebaseUserRepository", "❌ Firebase Exception: ${e.message}", e)
            throw Exception("Firebase error: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Registration failed: ${e.message}")
        }
    }

    // ✅ ADD THIS FUNCTION - Place it at the bottom of the FirebaseUserRepository class
    private suspend fun generateUniqueFriendCode(): String {
        var attempts = 0
        val maxAttempts = 10

        while (attempts < maxAttempts) {
            // Generate format:  STEP-XXXX (e. g., STEP-A3F9)
            // Using characters that are easy to read (no I, O, 0, 1)
            val code = "STEP-" + (1.. 4).map {
                "ABCDEFGHJKLMNPQRSTUVWXYZ23456789". random()
            }.joinToString("")

            // Check if code already exists in Firestore
            val existing = usersCollection
                .whereEqualTo("friendCode", code)
                .limit(1)
                .get()
                .await()

            if (existing. isEmpty) {
                android.util.Log.d("FirebaseUserRepository", "Generated unique friend code: $code")
                return code
            }

            attempts++
            android.util.Log.d("FirebaseUserRepository", "Friend code collision, retrying...  (attempt $attempts)")
        }

        // Fallback: use timestamp-based code if all random attempts failed (very unlikely)
        val timestamp = System.currentTimeMillis().toString().takeLast(4)
        val fallbackCode = "STEP-$timestamp"
        android.util. Log.w("FirebaseUserRepository", "Using fallback friend code: $fallbackCode")
        return fallbackCode
    }

    override suspend fun updateUser(user: User) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            val userDoc = hashMapOf(
                "email" to user.email,
                "name" to user.name,
                "friendCode" to user.friendCode,
                "age" to user.age,
                "weight" to user.weight,
                "height" to user. height,
                "fitnessGoal" to user.fitnessGoal,
                "dailyStepGoal" to user.dailyStepGoal,
                "updatedAt" to System.currentTimeMillis()
            )

            usersCollection.document(currentUser.uid).update(userDoc as Map<String, Any>).await()
        } catch (e: Exception) {
            throw Exception("Failed to update user: ${e.message}")
        }
    }

    override suspend fun updateDailyStepGoal(userId: String, goal: Int) {
        val currentUser = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            usersCollection.document(currentUser.uid)
                .update(
                    mapOf(
                        "dailyStepGoal" to goal,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update goal: ${e.message}")
        }
    }

    override suspend fun authenticateUser(email: String, password: String): User? {
        return try {
            // Sign in with Firebase Auth
            val authResult = auth. signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Authentication failed")

            // Get user document from Firestore
            val document = usersCollection.document(firebaseUser.uid).get().await()

            if (document.exists()) {
                val friendCode = document. getString("friendCode")

                // ✅ Generate friend code if missing (for old users)
                if (friendCode.isNullOrEmpty()) {
                    val newFriendCode = generateUniqueFriendCode()
                    usersCollection. document(firebaseUser.uid)
                        .update("friendCode", newFriendCode)
                        .await()
                    android.util.Log.d("FirebaseUserRepository", "✅ Generated friend code on login: $newFriendCode")
                }

                document.toObject(User::class.java)?.copy(
                    id = firebaseUser.uid
                )
            } else {
                throw Exception("User profile not found in database")
            }
        } catch (e: FirebaseAuthException) {
            // Propagate the actual Firebase error
            throw Exception("Login failed: ${e. message}")
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    // ✅ Helper function to generate unique friend code
    private suspend fun generateFriendCode(): String {
        var code:  String
        var isUnique = false

        while (! isUnique) {
            // Generate format: STEP-XXXX (e.g., STEP-A3F9)
            code = "STEP-" + (1.. 4).map {
                "ABCDEFGHJKLMNPQRSTUVWXYZ23456789". random()
            }.joinToString("")

            // Check if code already exists
            val existing = usersCollection
                .whereEqualTo("friendCode", code)
                .limit(1)
                .get()
                .await()

            if (existing.isEmpty) {
                return code
            }
        }

        return "STEP-0000" // Fallback (should never happen)
    }
}