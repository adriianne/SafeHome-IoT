package com.example.safehome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safehome.adapters.DeviceAdapter
import com.example.safehome.auth.AuthViewModel
import com.example.safehome.LoginActivity
import com.example.safehome.data.Device

class HomeActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter
    private val devices = mutableListOf<Device>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvDeviceCount = findViewById<TextView>(R.id.tvDeviceCount)
        val tvPowerUsage = findViewById<TextView>(R.id.tvPowerUsage)
        val btnAddDevice = findViewById<Button>(R.id.btnAddDevice)
        val btnScanDevice = findViewById<Button>(R.id.btnScanDevice)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        val btnSignOut = findViewById<Button>(R.id.btnSignOut)
        val rvDevices = findViewById<RecyclerView>(R.id.rvDevices)

        // Set user info
        val user = viewModel.currentUserName
        val email = viewModel.currentUserEmail
        tvWelcome.text = if (user.isNullOrBlank()) "Welcome! 👋" else "Welcome, $user! 👋"
        tvEmail.text = email ?: "user@example.com"

        // Setup RecyclerView
        deviceAdapter = DeviceAdapter(
            devices = devices,
            onToggleClick = { device -> toggleDevice(device) },
            onRemoveClick = { device -> removeDevice(device) }
        )
        rvDevices.layoutManager = LinearLayoutManager(this)
        rvDevices.adapter = deviceAdapter

        // Load sample devices
        loadSampleDevices()

        // Update stats
        updateStats()

        // Button listeners
        btnAddDevice.setOnClickListener {
            Toast.makeText(this, "Add Device feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        btnScanDevice.setOnClickListener {
            Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show()
        }

        btnSettings.setOnClickListener {
            Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        btnSignOut.setOnClickListener {
            viewModel.signOut()
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun loadSampleDevices() {
        devices.clear()
        devices.addAll(listOf(
            Device(id = "1", name = "Living Room Light", isConnected = true, powerUsage = 60.0, isOn = true),
            Device(id = "2", name = "Kitchen Fan", isConnected = true, powerUsage = 45.0, isOn = false),
            Device(id = "3", name = "Smart Plug", isConnected = false, powerUsage = 0.0, isOn = false)
        ))
        deviceAdapter.updateDevices(devices)
    }

    private fun toggleDevice(device: Device) {
        val index = devices.indexOfFirst { it.id == device.id }
        if (index != -1) {
            val updated = device.copy(isOn = !device.isOn)
            devices[index] = updated
            deviceAdapter.updateDevices(devices)
            updateStats()

            val status = if (updated.isOn) "ON" else "OFF"
            Toast.makeText(this, "${device.name} turned $status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeDevice(device: Device) {
        devices.removeAll { it.id == device.id }
        deviceAdapter.updateDevices(devices)
        updateStats()
        Toast.makeText(this, "${device.name} removed", Toast.LENGTH_SHORT).show()
    }

    private fun updateStats() {
        val count = devices.size
        val totalPower = devices.filter { it.isConnected }.sumOf { it.powerUsage }

        findViewById<TextView>(R.id.tvDeviceCount).text = count.toString()
        findViewById<TextView>(R.id.tvPowerUsage).text = "${totalPower.toInt()}W"
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}