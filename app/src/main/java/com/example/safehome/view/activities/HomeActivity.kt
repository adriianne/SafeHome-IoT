package com.example.safehome.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safehome.R
import com.example.safehome.contract.HomeContract
import com.example.safehome.model.Device
import com.example.safehome.presenter.HomePresenter
import com.example.safehome.view.activities.LoginActivity
import com.example.safehome.view.adapters.DeviceAdapter

class HomeActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var presenter: HomeContract.Presenter
    private lateinit var deviceAdapter: DeviceAdapter

    private lateinit var tvWelcome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvDeviceCount: TextView
    private lateinit var tvPowerUsage: TextView
    private lateinit var btnAddDevice: Button
    private lateinit var btnScanDevice: Button
    private lateinit var btnSettings: Button
    private lateinit var btnSignOut: Button
    private lateinit var rvDevices: RecyclerView
    private lateinit var progressHome: ProgressBar

    private val devices = mutableListOf<Device>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        presenter = HomePresenter(this)
        initViews()
        setupRecyclerView()
        setupListeners()

        // Load data
         presenter.loadUserData()
        presenter.loadDevices()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvEmail = findViewById(R.id.tvEmail)
        tvDeviceCount = findViewById(R.id.tvDeviceCount)
        tvPowerUsage = findViewById(R.id.tvPowerUsage)
        btnAddDevice = findViewById(R.id.btnAddDevice)
        btnScanDevice = findViewById(R.id.btnScanDevice)
        btnSettings = findViewById(R.id.btnSettings)
        btnSignOut = findViewById(R.id.btnSignOut)
        rvDevices = findViewById(R.id.rvDevices)
        progressHome = findViewById(R.id.progressHome)
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(
            devices = devices,
            onToggleClick = { device -> presenter.toggleDevice(device) },
            onRemoveClick = { device -> presenter.removeDevice(device) }
        )
        rvDevices.layoutManager = LinearLayoutManager(this)
        rvDevices.adapter = deviceAdapter
    }

    private fun setupListeners() {
        btnAddDevice.setOnClickListener {
            Toast.makeText(this, "Add Device feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        btnScanDevice.setOnClickListener {
            Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show()
        }

        btnSettings.setOnClickListener {
            navigateToSettings()
        }

        btnSignOut.setOnClickListener {
            presenter.signOut()
        }
    }

    // ===== HOME CONTRACT.VIEW IMPLEMENTATIONS =====

    override fun showLoading() {
        progressHome.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressHome.visibility = View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showDevices(devices: List<Device>) {
        this.devices.clear()
        this.devices.addAll(devices)
        deviceAdapter.updateDevices(this.devices)
    }

    override fun updateDeviceCount(count: Int) {
        tvDeviceCount.text = count.toString()
    }

    override fun updatePowerUsage(power: Double) {
        tvPowerUsage.text = "${power.toInt()}W"
    }

    override fun showWelcomeMessage(name: String) {
        tvWelcome.text = "Welcome, $name! 👋"
    }

    override fun showUserEmail(email: String) {
        tvEmail.text = email
    }

    override fun showDeviceToggled(deviceName: String, isOn: Boolean) {
        val status = if (isOn) "ON" else "OFF"
        Toast.makeText(this, "$deviceName turned $status", Toast.LENGTH_SHORT).show()
    }

    override fun showDeviceRemoved(deviceName: String) {
        Toast.makeText(this, "$deviceName removed", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun navigateToChangePassword() {
        // Deprecated in favor of Settings flow.
        // Kept to satisfy the HomeContract.View interface.
        // Settings is the new entry point for password change.
    }

    private fun navigateToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}