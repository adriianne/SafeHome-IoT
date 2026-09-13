package com.example.safehome.presenter

import com.example.safehome.contract.ChangePasswordContract
import com.example.safehome.model.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangePasswordPresenter(
    private val view: ChangePasswordContract.View,
    private val repository: AuthRepository = AuthRepository()
) : ChangePasswordContract.Presenter {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun validateAndChangePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        // Validate Current Password
        if (currentPassword.isBlank()) {
            view.showCurrentPasswordError("Current password is required")
            return
        }
        view.showCurrentPasswordError("")

        // Validate New Password Complexity
        if (newPassword.isBlank()) {
            view.showNewPasswordError("New password is required")
            return
        }
        if (newPassword.length < 8) {
            view.showNewPasswordError("Password must be at least 8 characters")
            return
        }
        if (!newPassword.any { it.isUpperCase() }) {
            view.showNewPasswordError("Must contain at least one uppercase letter")
            return
        }
        if (!newPassword.any { it.isLowerCase() }) {
            view.showNewPasswordError("Must contain at least one lowercase letter")
            return
        }
        if (!newPassword.any { it.isDigit() }) {
            view.showNewPasswordError("Must contain at least one number")
            return
        }
        if (!newPassword.any { !it.isLetterOrDigit() }) {
            view.showNewPasswordError("Must contain at least one special character")
            return
        }
        if (newPassword == currentPassword) {
            view.showNewPasswordError("New password must be different from current")
            return
        }
        view.showNewPasswordError("")

        // Validate Confirm Password
        if (confirmPassword.isBlank()) {
            view.showConfirmPasswordError("Please confirm your password")
            return
        }
        if (newPassword != confirmPassword) {
            view.showConfirmPasswordError("Passwords do not match")
            return
        }
        view.showConfirmPasswordError("")

        // Perform Change Password
        view.showLoading()
        scope.launch {
            val result = repository.changePassword(currentPassword, newPassword)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                result.fold(
                    onSuccess = {
                        view.showSuccess("Password changed successfully!")
                        view.navigateToHome()
                    },
                    onFailure = { error ->
                        view.showError(error.message ?: "Failed to change password")
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
    }
}