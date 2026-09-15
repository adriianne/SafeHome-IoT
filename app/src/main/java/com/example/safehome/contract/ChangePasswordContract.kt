package com.example.safehome.contract

interface ChangePasswordContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showSuccess(message: String)
        fun showCurrentPasswordError(message: String)
        fun showNewPasswordError(message: String)
        fun showConfirmPasswordError(message: String)
        fun navigateToHome()
    }

    interface Presenter {
        fun validateAndChangePassword(
            currentPassword: String,
            newPassword: String,
            confirmPassword: String
        )
        fun onDestroy()
    }
}