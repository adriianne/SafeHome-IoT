package com.example.safehome.contract

import com.example.safehome.model.Device

/**
 * MVP Contract for Home functionality
 */
interface HomeContract {
    
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showWelcomeMessage(name: String)
        fun showUserEmail(email: String)
        fun showDevices(devices: List<Device>)
        fun showDeviceToggled(deviceName: String, isOn: Boolean)
        fun showDeviceRemoved(deviceName: String)
        fun updateDeviceCount(count: Int)
        fun updatePowerUsage(power: Double)
        fun navigateToLogin()
        fun navigateToSettings()
        fun navigateToAddDevice()
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
