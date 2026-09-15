package com.example.safehome.model.repository

import com.example.safehome.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for Authentication operations
 * Handles Firebase Authentication and User data management
 */
class AuthRepository {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("User not found"))
            
            // Fetch user data from Firestore
            val user = getUserData(firebaseUser.uid) ?: User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: email.substringBefore("@")
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Register a new user
     */
    suspend fun register(
        email: String, 
        password: String, 
        firstName: String, 
        lastName: String
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Registration failed"))
            
            // Update display name
            val displayName = "$firstName $lastName"
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            
            // Create user document in Firestore
            val user = User(
                id = firebaseUser.uid,
                firstName = firstName,
                lastName = lastName,
                email = email,
                displayName = displayName,
                createdAt = System.currentTimeMillis()
            )
            
            usersCollection.document(firebaseUser.uid).set(user).await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Alias for register - used by SignupPresenter
     */
    suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<User> = register(email, password, firstName, lastName)
    
    /**
     * Get current logged in user
     */
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let {
            getUserData(it.uid) ?: User(
                id = it.uid,
                email = it.email ?: "",
                displayName = it.displayName ?: ""
            )
        }
    }
    
    /**
     * Get user data from Firestore
     */
    private suspend fun getUserData(userId: String): User? {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Logout current user
     */
    fun logout() {
        auth.signOut()
    }
    
    /**
     * Alias for logout - used by HomePresenter and SettingsPresenter
     */
    fun signOut() = logout()
    
    /**
     * Reset password
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Alias for resetPassword - used by ForgotPasswordPresenter
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = resetPassword(email)
    
    /**
     * Change password
     * Note: Firebase doesn't support changing password with current password verification directly.
     * This requires re-authentication first. For simplicity, we just update the password.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            
            // Re-authenticate user with current password before allowing change
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.email ?: "", currentPassword)
            
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
