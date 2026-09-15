package com.example.safehome.model.repository

import android.util.Log
import com.example.safehome.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // ============================================================
    // SIGNUP — Using suspendCancellableCoroutine instead of await()
    // ============================================================
    suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<User> = suspendCancellableCoroutine { cont ->
        Log.d("AuthRepository", "1. Calling createUserWithEmailAndPassword...")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                Log.d("AuthRepository", "2. Auth success. UID: ${authResult.user?.uid}")

                val firebaseUser = authResult.user
                val uid = firebaseUser?.uid

                if (firebaseUser == null || uid == null) {
                    Log.e("AuthRepository", "3. No user in result")
                    if (cont.isActive) cont.resume(Result.failure(Exception("User creation failed")))
                    return@addOnSuccessListener
                }

                // Build user object
                val user = User(
                    id = uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    displayName = "$firstName $lastName"
                )

                // ===== UPDATE PROFILE (fire and forget) =====
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName("$firstName $lastName")
                    .build()

                firebaseUser.updateProfile(profileUpdates)
                    .addOnSuccessListener {
                        Log.d("AuthRepository", "3. Profile display name updated")
                    }
                    .addOnFailureListener { e ->
                        Log.e("AuthRepository", "Profile update failed: ${e.message}")
                    }

                // ===== SAVE TO DB (fire and forget) =====
                val profile = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )

                database.child("users").child(uid).setValue(profile)
                    .addOnSuccessListener {
                        Log.d("AuthRepository", "4. DB save complete")
                    }
                    .addOnFailureListener { e ->
                        Log.e("AuthRepository", "DB save failed: ${e.message}")
                    }

                // ===== ✅ RESUME IMMEDIATELY AFTER AUTH SUCCESS =====
                // Don't wait for profile update or DB save.
                Log.d("AuthRepository", "5. Resuming coroutine with success")
                if (cont.isActive) cont.resume(Result.success(user))
            }
            .addOnFailureListener { e ->
                Log.e("AuthRepository", "Signup FAILED: ${e.message}", e)
                if (cont.isActive) cont.resume(Result.failure(e))
            }
    }

    // ============================================================
    // LOGIN — Using suspendCancellableCoroutine
    // ============================================================
    suspend fun login(email: String, password: String): Result<User> = suspendCancellableCoroutine { cont ->
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: ""
                    )
                    cont.resume(Result.success(user))
                } else {
                    cont.resume(Result.failure(Exception("Login failed")))
                }
            }
            .addOnFailureListener { e ->
                cont.resume(Result.failure(e))
            }
    }

    // ============================================================
    // SEND PASSWORD RESET — Using suspendCancellableCoroutine
    // ============================================================
    suspend fun sendPasswordReset(email: String): Result<Unit> = suspendCancellableCoroutine { cont ->
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                cont.resume(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                cont.resume(Result.failure(e))
            }
    }

    // ============================================================
    // CHANGE PASSWORD — Using suspendCancellableCoroutine
    // ============================================================
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            val user = auth.currentUser
            val email = user?.email

            if (user == null || email == null) {
                cont.resume(Result.failure(Exception("User not logged in")))
                return@suspendCancellableCoroutine
            }

            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            cont.resume(Result.success(Unit))
                        }
                        .addOnFailureListener { e ->
                            cont.resume(Result.failure(e))
                        }
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }

    // ============================================================
    // HELPER FUNCTIONS (non-suspend)
    // ============================================================
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        val displayName = firebaseUser.displayName ?: ""
        val parts = displayName.split(" ")
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = displayName,
            firstName = parts.firstOrNull() ?: "",
            lastName = parts.lastOrNull() ?: ""
        )
    }

    fun signOut() {
        auth.signOut()
    }
}