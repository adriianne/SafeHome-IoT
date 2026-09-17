package com.example.safehome.contract

import com.example.safehome.model.Device
import com.example.safehome.model.Telemetry
import com.example.safehome.model.WasteStatus

interface HomeContract {

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)

        // Identity
        fun showWelcomeMessage(name: String)
        fun showUserEmail(email: String)

        // Live node state
        fun showTelemetry(telemetry: Telemetry)
        fun showEnergyToday(kwh: Double, costPhp: Double)
        fun showOccupancy(motion: Boolean, idleMinutes: Int)
        fun showDeviceOffline(offline: Boolean)

        // Waste detection
        fun showWasteBanner(waste: WasteStatus, channelNames: List<String>)
        fun hideWasteBanner()

        // Channels
        fun showDevices(devices: List<Device>)
        fun showDeviceToggled(deviceName: String, isOn: Boolean)
        fun showAllChannelsOff()

        // Navigation
        fun navigateToLogin()
        fun navigateToSettings()
        fun navigateToHistory()
        fun navigateToChangePassword()
    }

    interface Presenter {
        fun loadUserData()
        fun startListening()
        fun stopListening()
        fun toggleDevice(device: Device)
        fun turnAllOff()
        fun signOut()
        fun onDestroy()
    }
}