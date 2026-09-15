package com.example.safehome.contract

import com.example.safehome.model.User

interface SignupContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun hideError()
        fun showFirstNameError(message: String)
        fun showLastNameError(message: String)
        fun showEmailError(message: String)
        fun showPasswordError(message: String)
        fun showConfirmPasswordError(message: String)
        fun onSignupSuccess(user: User)
        fun navigateToHome()
    }

    interface Presenter {
        fun validateAndSignup(
            firstName: String,
            lastName: String,
            email: String,
            password: String,
            confirmPassword: String
        )
        fun onDestroy()
    }
}