package com.example.safehome.contract

interface SettingsContract {
    interface View {
        fun showUserInfo(name: String, email: String)
        fun showError(message: String)
        fun navigateToChangePassword()
        fun navigateToLogin()
    }

    interface Presenter {
        fun loadUserInfo()
        fun onChangePasswordClicked()
        fun onSignOutClicked()
        fun onDestroy()
    }
}