package com.example.safehome.contract

/**
 * MVP Contract for Settings functionality
 */
interface SettingsContract {
    
    interface View {
        fun showUserInfo(name: String, email: String)
        fun navigateToLogin()
        fun navigateToChangePassword()
    }
    
    interface Presenter {
        fun loadUserInfo()
        fun onChangePasswordClicked()
        fun onSignOutClicked()
        fun onDestroy()
    }
}
