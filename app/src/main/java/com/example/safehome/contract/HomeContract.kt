package com.example.safehome.contract

import com.example.safehome.model.Device

interface HomeContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showDevices(devices: List<Device>)
        fun updateDeviceCount(count: Int)
        fun updatePowerUsage(power: Double)
        fun showWelcomeMessage(name: String)
        fun showUserEmail(email: String)
        fun showDeviceToggled(deviceName: String, isOn: Boolean)
        fun showDeviceRemoved(deviceName: String)
        fun navigateToLogin()
        fun navigateToChangePassword()
    }

    interface Presenter {
        fun loadUserData()
        fun loadDevices()
        fun toggleDevice(device: Device)
        fun removeDevice(device: Device)
        fun signOut()
        fun onDestroy()
    }
}