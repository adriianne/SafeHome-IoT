package com.example.safehome.contract

interface ForgotPasswordContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showSuccess(message: String)
        fun showEmailError(message: String)
        fun navigateToLogin()
    }

    interface Presenter {
        fun validateAndSendResetEmail(email: String)
        fun onDestroy()
    }
}