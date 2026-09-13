package com.example.safehome.presenter

import android.util.Log
import com.example.safehome.contract.SignupContract
import com.example.safehome.model.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SignupPresenter(
    private val view: SignupContract.View,
    private val repository: AuthRepository = AuthRepository()
) : SignupContract.Presenter {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun validateAndSignup(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        // ===== VALIDATION =====
        if (firstName.isBlank()) {
            view.showFirstNameError("First name is required")
            return
        }
        if (firstName.length < 2) {
            view.showFirstNameError("First name must be at least 2 characters")
            return
        }
        view.showFirstNameError("")

        if (lastName.isBlank()) {
            view.showLastNameError("Last name is required")
            return
        }
        if (lastName.length < 2) {
            view.showLastNameError("Last name must be at least 2 characters")
            return
        }
        view.showLastNameError("")

        if (email.isBlank()) {
            view.showEmailError("Email is required")
            return
        }
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            view.showEmailError("Invalid email format")
            return
        }
        view.showEmailError("")

        if (password.isBlank()) {
            view.showPasswordError("Password is required")
            return
        }
        if (password.length < 8) {
            view.showPasswordError("Password must be at least 8 characters")
            return
        }
        if (!password.any { it.isUpperCase() }) {
            view.showPasswordError("Must contain at least one uppercase letter")
            return
        }
        if (!password.any { it.isLowerCase() }) {
            view.showPasswordError("Must contain at least one lowercase letter")
            return
        }
        if (!password.any { it.isDigit() }) {
            view.showPasswordError("Must contain at least one number")
            return
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            view.showPasswordError("Must contain at least one special character")
            return
        }
        view.showPasswordError("")

        if (confirmPassword.isBlank()) {
            view.showConfirmPasswordError("Please confirm your password")
            return
        }
        if (password != confirmPassword) {
            view.showConfirmPasswordError("Passwords do not match")
            return
        }
        view.showConfirmPasswordError("")

        // ===== PERFORM SIGNUP =====
        view.showLoading()
        scope.launch {
            try {
                Log.d("SignupPresenter", "Starting signup for: $email")

                val result = withTimeoutOrNull(30_000L) {
                    repository.signup(firstName, lastName, email, password)
                }

                withContext(Dispatchers.Main) {
                    if (result == null) {
                        Log.e("SignupPresenter", "TIMEOUT")
                        view.hideLoading()   // ✅ ADDED
                        view.showError("Request timed out. Please try again.")
                    } else {
                        result.fold(
                            onSuccess = { user ->
                                Log.d("SignupPresenter", "Signup SUCCESS for ${user.email}")
                                view.onSignupSuccess(user)
                                view.hideLoading()   // ✅ On success
                                view.navigateToHome()
                            },
                            onFailure = { error ->
                                Log.e("SignupPresenter", "Signup FAILED: ${error.message}")
                                view.hideLoading()   // ✅ ADDED — on failure
                                view.showError(error.message ?: "Signup failed")
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SignupPresenter", "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    view.hideLoading()   // ✅ ADDED
                    view.showError("Unexpected error: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    view.hideLoading()   // ✅ Safety net — always runs
                }
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
    }
}