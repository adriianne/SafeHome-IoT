package com.example.safehome.data

data class Device(
    val id: String = "",
    val name: String = "",
    val macAddress: String = "",
    val isConnected: Boolean = false,
    val powerUsage: Double = 0.0,
    val isOn: Boolean = false,
    val type: String = "smart_plug"
)