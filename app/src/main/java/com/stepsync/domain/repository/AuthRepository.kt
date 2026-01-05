package com.stepsync.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Authentication operations
 * Provides authentication state observation
 */
interface AuthRepository {
    /**
     * Flow that emits the current authenticated user or null if not authenticated
     */
    fun observeAuthState(): Flow<FirebaseUser?>
    
    /**
     * Returns the current authenticated user synchronously
     */
    fun getCurrentUser(): FirebaseUser?
    
    /**
     * Returns true if a user is currently authenticated
     */
    fun isUserAuthenticated(): Boolean
}
