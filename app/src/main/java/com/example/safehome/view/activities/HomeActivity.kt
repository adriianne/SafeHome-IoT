package com.example.safehome.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safehome.R
import com.example.safehome.contract.HomeContract
import com.example.safehome.model.Device
import com.example.safehome.model.Telemetry
import com.example.safehome.model.WasteStatus
import com.example.safehome.presenter.HomePresenter
import com.example.safehome.view.adapters.DeviceAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class HomeActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var presenter: HomeContract.Presenter
    private lateinit var deviceAdapter: DeviceAdapter

    // Identity
    private lateinit var tvWelcome: TextView
    private lateinit var tvEmail: TextView

    // Live meter
    private lateinit var tvLivePower: TextView
    private lateinit var tvMeterDetail: TextView

    // Stats
    private lateinit var tvEnergyToday: TextView
    private lateinit var tvCostToday: TextView
    private lateinit var tvOccupancy: TextView
    private lateinit var tvAmbient: TextView

    // Waste banner and offline notice
    private lateinit var cardWaste: MaterialCardView
    private lateinit var tvWasteDetail: TextView
    private lateinit var btnWasteTurnOff: MaterialButton
    private lateinit var cardOffline: MaterialCardView

    // Channels and actions
    private lateinit var rvDevices: RecyclerView
    private lateinit var progressHome: ProgressBar
    private lateinit var btnAllOff: MaterialButton
    private lateinit var btnHistory: MaterialButton
    private lateinit var btnSignOut: MaterialButton
    private lateinit var btnSettings: ImageButton

    private val devices = mutableListOf<Device>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        presenter = HomePresenter(this)
        initViews()
        setupRecyclerView()
        setupListeners()

        presenter.loadUserData()
    }

    /**
     * Listeners are attached in onStart and detached in onStop rather than in
     * onCreate/onDestroy, so the app is not holding four open Firebase listeners
     * while it sits in the background.
     */
    override fun onStart() {
        super.onStart()
        presenter.startListening()
    }

    override fun onStop() {
        presenter.stopListening()
        super.onStop()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvEmail = findViewById(R.id.tvEmail)

        tvLivePower = findViewById(R.id.tvLivePower)
        tvMeterDetail = findViewById(R.id.tvMeterDetail)

        tvEnergyToday = findViewById(R.id.tvEnergyToday)
        tvCostToday = findViewById(R.id.tvCostToday)
        tvOccupancy = findViewById(R.id.tvOccupancy)
        tvAmbient = findViewById(R.id.tvAmbient)

        cardWaste = findViewById(R.id.cardWaste)
        tvWasteDetail = findViewById(R.id.tvWasteDetail)
        btnWasteTurnOff = findViewById(R.id.btnWasteTurnOff)
        cardOffline = findViewById(R.id.cardOffline)

        rvDevices = findViewById(R.id.rvDevices)
        progressHome = findViewById(R.id.progressHome)
        btnAllOff = findViewById(R.id.btnAllOff)
        btnHistory = findViewById(R.id.btnHistory)
        btnSignOut = findViewById(R.id.btnSignOut)
        btnSettings = findViewById(R.id.btnSettings)
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(
            devices = devices,
            onToggle = { device -> presenter.toggleDevice(device) }
        )
        rvDevices.layoutManager = LinearLayoutManager(this)
        rvDevices.adapter = deviceAdapter
        rvDevices.isNestedScrollingEnabled = false
    }

    private fun setupListeners() {
        btnAllOff.setOnClickListener { presenter.turnAllOff() }
        btnWasteTurnOff.setOnClickListener { presenter.turnAllOff() }
        btnHistory.setOnClickListener { navigateToHistory() }
        btnSettings.setOnClickListener { navigateToSettings() }
        btnSignOut.setOnClickListener { presenter.signOut() }
    }

    // ===== HomeContract.View =====

    override fun showLoading() {
        progressHome.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressHome.visibility = View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showWelcomeMessage(name: String) {
        tvWelcome.text = getString(R.string.welcome_user, name)
    }

    override fun showUserEmail(email: String) {
        tvEmail.text = email
    }

    override fun showTelemetry(telemetry: Telemetry) {
        tvLivePower.text = String.format(Locale.US, "%.1f", telemetry.power)
        tvMeterDetail.text = String.format(
            Locale.US,
            "%.1f V · %.2f A · PF %.2f",
            telemetry.voltage, telemetry.current, telemetry.powerFactor
        )
        tvAmbient.text = String.format(
            Locale.US, "%.1f°C · %.0f%%", telemetry.temperature, telemetry.humidity
        )
    }

    override fun showEnergyToday(kwh: Double, costPhp: Double) {
        tvEnergyToday.text = String.format(Locale.US, "%.2f kWh", kwh)
        tvCostToday.text = String.format(Locale.US, "₱%.2f", costPhp)
    }

    override fun showOccupancy(motion: Boolean, idleMinutes: Int) {
        if (motion) {
            tvOccupancy.text = getString(R.string.occupancy_motion)
            tvOccupancy.setTextColor(getColor(R.color.ok))
        } else {
            tvOccupancy.text = getString(R.string.occupancy_idle, idleMinutes)
            // Amber once the room has been still for a while, red is reserved for waste.
            tvOccupancy.setTextColor(
                getColor(if (idleMinutes >= 10) R.color.warn else R.color.muted)
            )
        }
    }

    override fun showDeviceOffline(offline: Boolean) {
        cardOffline.visibility = if (offline) View.VISIBLE else View.GONE
    }

    override fun showWasteBanner(waste: WasteStatus, channelNames: List<String>) {
        val appliances = when {
            channelNames.isEmpty() -> getString(R.string.waste_no_named_channels)
            channelNames.size == 1 -> channelNames.first()
            else -> channelNames.dropLast(1).joinToString(", ") +
                    " and " + channelNames.last()
        }

        tvWasteDetail.text = getString(
            R.string.waste_detail,
            waste.totalPower,
            waste.idleMinutes,
            appliances
        )
        cardWaste.visibility = View.VISIBLE

        // Mark the offending rows so the list agrees with the banner.
        deviceAdapter.setWastingChannels(waste.channelsOn.toSet())
    }

    override fun hideWasteBanner() {
        cardWaste.visibility = View.GONE
        deviceAdapter.setWastingChannels(emptySet())
    }

    override fun showDevices(devices: List<Device>) {
        this.devices.clear()
        this.devices.addAll(devices)
        deviceAdapter.updateDevices(this.devices)
    }

    override fun showDeviceToggled(deviceName: String, isOn: Boolean) {
        val status = if (isOn) "on" else "off"
        Toast.makeText(this, "Turning $deviceName $status…", Toast.LENGTH_SHORT).show()
    }

    override fun showAllChannelsOff() {
        Toast.makeText(this, "Turning everything off…", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun navigateToHistory() {
        // HistoryActivity is not built yet (FR-12). Replace this once it exists.
        Toast.makeText(this, "Consumption history is coming soon", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToChangePassword() {
        // Password change now lives in the Settings flow.
        // Kept to satisfy HomeContract.View.
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}