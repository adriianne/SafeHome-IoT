package com.example.safehome.contract

import com.example.safehome.model.User

/**
 * MVP Contract for Signup functionality
 */
interface SignupContract {
    
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showFirstNameError(message: String)
        fun showLastNameError(message: String)
        fun showEmailError(message: String)
        fun showPasswordError(message: String)
        fun showConfirmPasswordError(message: String)
        fun onSignupSuccess(user: User)
        fun navigateToHome()
        fun navigateToLogin()
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
