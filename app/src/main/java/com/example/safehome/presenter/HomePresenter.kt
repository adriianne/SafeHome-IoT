package com.example.safehome.presenter

import android.util.Log
import com.example.safehome.contract.HomeContract
import com.example.safehome.model.Device
import com.example.safehome.model.repository.AuthRepository
import com.example.safehome.model.repository.DeviceRepository

class HomePresenter(
    private val view: HomeContract.View,
    private val authRepository: AuthRepository = AuthRepository(),
    private val deviceRepository: DeviceRepository = DeviceRepository()
) : HomeContract.Presenter {

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

    override fun startListening() {
        view.showLoading()
        deviceRepository.listenForDevices { updatedDevices ->
            devices.clear()
            devices.addAll(updatedDevices)
            view.hideLoading()
            view.showDevices(devices)

            if (devices.isNotEmpty() && devices.none { it.isOn }) {
                view.showAllChannelsOff()
            }
        }
    }

    override fun stopListening() {
        deviceRepository.removeListener()
    }

    override fun toggleDevice(device: Device) {
        val command = if (device.isOn) "OFF" else "ON"
        Log.d("HomePresenter", "Sending $command to ${device.name}")

        val success = deviceRepository.sendCommand(device.id, command)
        if (success) {
            view.showDeviceToggled(device.name, !device.isOn)
        } else {
            view.showError("Failed to send command")
        }
    }

    override fun turnAllOff() {
        val success = deviceRepository.turnAllOff()
        if (success) {
            view.showAllChannelsOff()
        } else {
            view.showError("Failed to turn all off")
        }
    }

    override fun signOut() {
        authRepository.signOut()
        view.navigateToLogin()
    }

    override fun onDestroy() {
        deviceRepository.removeListener()
    }
}