package com.example.safehome.presenter

import com.example.safehome.contract.SettingsContract
import com.example.safehome.model.UserSettings
import com.example.safehome.model.repository.AuthRepository
import com.example.safehome.model.repository.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SettingsPresenter(
    private val view: SettingsContract.View,
    private val authRepository: AuthRepository = AuthRepository(),
    private val deviceRepository: DeviceRepository = DeviceRepository()
) : SettingsContract.Presenter {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun loadUserInfo() {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            view.navigateToLogin()
            return
        }
        val name = user.displayName.ifEmpty { "${user.firstName} ${user.lastName}".trim() }
        view.showUserInfo(name.ifEmpty { "User" }, user.email)
        view.showPairedDevice(DeviceRepository.DEFAULT_DEVICE_ID)
    }

    /**
     * Reads once rather than listening. A form should not change under the user
     * while they are typing in it.
     */
    override fun loadSettings() {
        scope.launch {
            try {
                view.showSettings(deviceRepository.readSettingsOnce())
            } catch (e: Exception) {
                view.showError("Couldn't load your settings")
            }
        }
    }

    override fun saveSettings(
        rate: String,
        idle: String,
        wasteThreshold: String,
        highDraw: String
    ) {
        val rateValue = rate.trim().toDoubleOrNull()
        val idleValue = idle.trim().toIntOrNull()
        val wasteValue = wasteThreshold.trim().toIntOrNull()
        val highValue = highDraw.trim().toIntOrNull()

        val rateError = when {
            rateValue == null -> "Enter a number"
            rateValue <= 0.0 -> "Rate must be greater than zero"
            rateValue > 100.0 -> "That looks too high for a per-kWh rate"
            else -> null
        }
        val idleError = when {
            idleValue == null -> "Enter a number"
            idleValue < 1 || idleValue > 120 -> "Use a value between 1 and 120"
            else -> null
        }
        val wasteError = when {
            wasteValue == null -> "Enter a number"
            wasteValue < 1 -> "Must be at least 1 W"
            else -> null
        }
        val highError = when {
            highValue == null -> "Enter a number"
            highValue < 100 -> "Must be at least 100 W"
            // A limit below the waste threshold would fire both alerts at once.
            wasteValue != null && highValue <= wasteValue ->
                "Must be higher than the waste threshold"
            else -> null
        }

        view.showRateError(rateError)
        view.showIdleError(idleError)
        view.showWasteThresholdError(wasteError)
        view.showHighDrawError(highError)

        if (listOfNotNull(rateError, idleError, wasteError, highError).isNotEmpty()) return

        scope.launch {
            view.showSaving(true)
            try {
                deviceRepository.saveSettings(
                    UserSettings(
                        electricityRatePhp = rateValue!!,
                        idleThresholdMinutes = idleValue!!,
                        wastePowerThresholdW = wasteValue!!,
                        highDrawLimitW = highValue!!
                    )
                )
                view.showSaveSuccess()
            } catch (e: Exception) {
                view.showError("Couldn't save. Check your connection.")
            } finally {
                view.showSaving(false)
            }
        }
    }

    override fun onChangePasswordClicked() {
        view.navigateToChangePassword()
    }

    override fun onSignOutClicked() {
        authRepository.signOut()
        view.navigateToLogin()
    }

    override fun onDestroy() {
        deviceRepository.stopAllListeners()
        job.cancel()
    }
}