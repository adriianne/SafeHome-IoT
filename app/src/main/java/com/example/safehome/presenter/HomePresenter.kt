package com.example.safehome.presenter

import com.example.safehome.contract.HomeContract
import com.example.safehome.model.Device
import com.example.safehome.model.Telemetry
import com.example.safehome.model.UserSettings
import com.example.safehome.model.WasteStatus
import com.example.safehome.model.repository.AuthRepository
import com.example.safehome.model.repository.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomePresenter(
    private val view: HomeContract.View,
    private val authRepository: AuthRepository = AuthRepository(),
    private val deviceRepository: DeviceRepository = DeviceRepository()
) : HomeContract.Presenter {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private var channels = listOf<Device>()
    private var settings = UserSettings()
    private var telemetry = Telemetry()

    override fun loadUserData() {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            view.navigateToLogin()
            return
        }
        val name = user.displayName.ifEmpty { "${user.firstName} ${user.lastName}".trim() }
        view.showWelcomeMessage(name.ifEmpty { "User" })
        view.showUserEmail(user.email)
    }

    /**
     * Attaches the live listeners. The node publishes every five seconds and on every
     * state change, so the screen redraws itself rather than being refreshed by the user.
     */
    override fun startListening() {
        view.showLoading()

        deviceRepository.listenToSettings(
            onChange = { s ->
                settings = s
                renderEnergy()
            },
            onError = { view.showError(it) }
        )

        deviceRepository.listenToTelemetry(
            onChange = { t ->
                view.hideLoading()
                telemetry = t
                view.showTelemetry(t)
                view.showOccupancy(t.motion, t.idleMinutes)
                view.showDeviceOffline(!t.online)
                renderEnergy()
            },
            onError = {
                view.hideLoading()
                view.showError(it)
            }
        )

        deviceRepository.listenToChannels(
            onChange = { list ->
                channels = list
                view.showDevices(list)
            },
            onError = { view.showError(it) }
        )

        deviceRepository.listenToWaste(
            onChange = { waste ->
                if (waste.detected) {
                    // Turn stored channel ids into the names the user actually recognises.
                    val names = waste.channelsOn.mapNotNull { id ->
                        channels.firstOrNull { it.id == id }?.name
                    }
                    view.showWasteBanner(waste, names)
                } else {
                    view.hideWasteBanner()
                }
            },
            onError = { view.showError(it) }
        )
    }

    override fun stopListening() {
        deviceRepository.stopAllListeners()
    }

    private fun renderEnergy() {
        val cost = telemetry.energyKwh * settings.electricityRatePhp
        view.showEnergyToday(telemetry.energyKwh, cost)
    }

    /**
     * Writes a command and stops. The node switches the relay and then writes the
     * resulting state back, so the switch in the UI moves after the relay has actually
     * moved — not when the user tapped it.
     */
    override fun toggleDevice(device: Device) {
        scope.launch {
            try {
                deviceRepository.sendCommand(device.id, if (device.isOn) "OFF" else "ON")
                view.showDeviceToggled(device.name, !device.isOn)
            } catch (e: Exception) {
                view.showError("Couldn't reach the device. Check your connection.")
            }
        }
    }

    override fun turnAllOff() {
        scope.launch {
            try {
                deviceRepository.sendCommand(target = null, action = "ALL_OFF")
                view.showAllChannelsOff()
            } catch (e: Exception) {
                view.showError("Couldn't reach the device. Check your connection.")
            }
        }
    }

    override fun signOut() {
        stopListening()
        authRepository.signOut()
        view.navigateToLogin()
    }

    override fun onDestroy() {
        stopListening()
        job.cancel()
    }
}