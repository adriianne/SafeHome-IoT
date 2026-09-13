package com.example.safehome.presenter

import com.example.safehome.contract.HomeContract
import com.example.safehome.model.Device
import com.example.safehome.model.repository.AuthRepository
import com.example.safehome.model.repository.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomePresenter(
    private val view: HomeContract.View,
    private val authRepository: AuthRepository = AuthRepository(),
    private val deviceRepository: DeviceRepository = DeviceRepository()
) : HomeContract.Presenter {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var devices = mutableListOf<Device>()

    override fun loadUserData() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            val name = user.displayName.ifEmpty {
                "${user.firstName} ${user.lastName}".trim()
            }
            view.showWelcomeMessage(name.ifEmpty { "User" })
            view.showUserEmail(user.email)
        } else {
            view.navigateToLogin()
        }
    }

    override fun loadDevices() {
        view.showLoading()

        // Load sample devices for now (replace with Firebase later)
        devices = mutableListOf(
            Device(id = "1", name = "Living Room Light", isConnected = true, powerUsage = 60.0, isOn = true),
            Device(id = "2", name = "Kitchen Fan", isConnected = true, powerUsage = 45.0, isOn = false),
            Device(id = "3", name = "Smart Plug", isConnected = false, powerUsage = 0.0, isOn = false)
        )

        view.hideLoading()
        view.showDevices(devices)
        updateStats()
    }

    override fun toggleDevice(device: Device) {
        val updatedDevice = device.copy(isOn = !device.isOn)
        val index = devices.indexOfFirst { it.id == device.id }
        if (index != -1) {
            devices[index] = updatedDevice
        }

        view.showDevices(devices)
        view.showDeviceToggled(device.name, updatedDevice.isOn)
        updateStats()
    }

    override fun removeDevice(device: Device) {
        devices.removeAll { it.id == device.id }
        view.showDevices(devices)
        view.showDeviceRemoved(device.name)
        updateStats()
    }

    private fun updateStats() {
        val count = devices.size
        val totalPower = devices.filter { it.isConnected }.sumOf { it.powerUsage }
        view.updateDeviceCount(count)
        view.updatePowerUsage(totalPower)
    }

    override fun signOut() {
        authRepository.signOut()
        view.navigateToLogin()
    }

    override fun onDestroy() {
        job.cancel()
    }
}