package com.example.safehome.contract

import com.example.safehome.model.User

/**
 * MVP Contract for Login functionality
 * Defines the interface between View, Presenter, and Repository
 */
interface LoginContract {
    
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun hideError()
        fun showEmailError(message: String)
        fun showPasswordError(message: String)
        fun onLoginSuccess(user: User)
        fun navigateToHome()
        fun navigateToSignup()
        fun navigateToForgotPassword()
    }
    
    interface Presenter {
        fun validateAndLogin(email: String, password: String)
        fun onDestroy()
    }
}
