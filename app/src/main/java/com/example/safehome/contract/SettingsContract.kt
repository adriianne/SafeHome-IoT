package com.example.safehome.contract

import com.example.safehome.model.UserSettings

interface SettingsContract {

    interface View {
        // Account
        fun showUserInfo(name: String, email: String)
        fun showPairedDevice(deviceId: String)

        // Detection thresholds (FR-15)
        fun showSettings(settings: UserSettings)
        fun showRateError(message: String?)
        fun showIdleError(message: String?)
        fun showWasteThresholdError(message: String?)
        fun showHighDrawError(message: String?)
        fun showSaving(saving: Boolean)
        fun showSaveSuccess()

        fun showError(message: String)
        fun navigateToChangePassword()
        fun navigateToLogin()
    }

    interface Presenter {
        fun loadUserInfo()
        fun loadSettings()
        fun saveSettings(rate: String, idle: String, wasteThreshold: String, highDraw: String)
        fun onChangePasswordClicked()
        fun onSignOutClicked()
        fun onDestroy()
    }
}