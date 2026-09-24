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

class HomeActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var presenter: HomeContract.Presenter
    private lateinit var deviceAdapter: DeviceAdapter
    private val devices = mutableListOf<Device>()

    // Header
    private lateinit var tvWelcome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnSettings: ImageButton

    // Offline / waste banners
    private lateinit var cardOffline: MaterialCardView
    private lateinit var cardWaste: MaterialCardView
    private lateinit var tvWasteDetail: TextView
    private lateinit var btnWasteTurnOff: MaterialButton

    // Live meter
    private lateinit var tvLivePower: TextView
    private lateinit var tvMeterDetail: TextView

    // Stats
    private lateinit var tvEnergyToday: TextView
    private lateinit var tvCostToday: TextView
    private lateinit var tvOccupancy: TextView
    private lateinit var tvAmbient: TextView

    // Channels
    private lateinit var rvDevices: RecyclerView
    private lateinit var progressHome: ProgressBar

    // Actions
    private lateinit var btnAllOff: MaterialButton
    private lateinit var btnHistory: MaterialButton
    private lateinit var btnSignOut: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        presenter = HomePresenter(this)
        initViews()
        setupRecyclerView()
        setupListeners()

        presenter.loadUserData()
        presenter.startListening()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvEmail = findViewById(R.id.tvEmail)
        btnSettings = findViewById(R.id.btnSettings)

        cardOffline = findViewById(R.id.cardOffline)
        cardWaste = findViewById(R.id.cardWaste)
        tvWasteDetail = findViewById(R.id.tvWasteDetail)
        btnWasteTurnOff = findViewById(R.id.btnWasteTurnOff)

        tvLivePower = findViewById(R.id.tvLivePower)
        tvMeterDetail = findViewById(R.id.tvMeterDetail)

        tvEnergyToday = findViewById(R.id.tvEnergyToday)
        tvCostToday = findViewById(R.id.tvCostToday)
        tvOccupancy = findViewById(R.id.tvOccupancy)
        tvAmbient = findViewById(R.id.tvAmbient)

        rvDevices = findViewById(R.id.rvDevices)
        progressHome = findViewById(R.id.progressHome)

        btnAllOff = findViewById(R.id.btnAllOff)
        btnHistory = findViewById(R.id.btnHistory)
        btnSignOut = findViewById(R.id.btnSignOut)
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(
            devices = devices,
            onToggleClick = { device -> presenter.toggleDevice(device) }
        )
        rvDevices.layoutManager = LinearLayoutManager(this)
        rvDevices.adapter = deviceAdapter
    }

    private fun setupListeners() {
        btnSettings.setOnClickListener { navigateToSettings() }
        btnSignOut.setOnClickListener { presenter.signOut() }
        btnAllOff.setOnClickListener { presenter.turnAllOff() }
        btnHistory.setOnClickListener { navigateToHistory() }
        btnWasteTurnOff.setOnClickListener { presenter.turnAllOff() }
    }

    // ═══════════════════════════════════════════════════════
    // HOME CONTRACT.VIEW IMPLEMENTATIONS
    // ═══════════════════════════════════════════════════════

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
        tvWelcome.text = "Welcome, $name"
    }

    override fun showUserEmail(email: String) {
        tvEmail.text = email
    }

    override fun showTelemetry(telemetry: Telemetry) {
        // Live power
        tvLivePower.text = String.format("%.1f", telemetry.power)

        // Voltage · Current · PF
        tvMeterDetail.text = String.format(
            "%.1f V · %.2f A · PF %.2f",
            telemetry.voltage,
            telemetry.current,
            telemetry.powerFactor
        )

        // Ambient temperature + humidity
        tvAmbient.text = String.format("%.1f°C · %.0f%%", telemetry.temperature, telemetry.humidity)

        // Energy today (if provided)
        showEnergyToday(telemetry.energyKwh, 0.0)
    }

    override fun showEnergyToday(kwh: Double, costPhp: Double) {
        tvEnergyToday.text = String.format("%.2f kWh", kwh)
        tvCostToday.text = String.format("₱%.2f", costPhp)
    }

    override fun showOccupancy(motion: Boolean, idleMinutes: Int) {
        tvOccupancy.text = if (motion) {
            "Motion detected"
        } else {
            "No motion · $idleMinutes min"
        }
    }

    override fun showDeviceOffline(offline: Boolean) {
        cardOffline.visibility = if (offline) View.VISIBLE else View.GONE
    }

    override fun showWasteBanner(waste: WasteStatus, channelNames: List<String>) {
        if (!waste.detected) {
            hideWasteBanner()
            return
        }

        val names = channelNames.joinToString(" and ")
        tvWasteDetail.text = String.format(
            "%.1f W is being drawn, but nothing has moved for %d minutes. %s.",
            waste.totalPower,
            waste.idleMinutes,
            names.ifEmpty { "Some channels" }
        )
        cardWaste.visibility = View.VISIBLE
    }

    override fun hideWasteBanner() {
        cardWaste.visibility = View.GONE
    }

    override fun showDevices(devices: List<Device>) {
        this.devices.clear()
        this.devices.addAll(devices)
        deviceAdapter.updateDevices(this.devices)
    }

    override fun showDeviceToggled(deviceName: String, isOn: Boolean) {
        val status = if (isOn) "ON" else "OFF"
        Toast.makeText(this, "$deviceName turned $status", Toast.LENGTH_SHORT).show()
    }

    override fun showAllChannelsOff() {
        // No toast — the UI already reflects this
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════

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
        Toast.makeText(this, "History coming soon", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToChangePassword() {
        startActivity(Intent(this, ChangePasswordActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}