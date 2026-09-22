package com.example.safehome.model

/**
 * The live snapshot published by the node, mirroring devices/{id}/telemetry.
 * These are node-level readings, not per-appliance readings.
 */
data class Telemetry(
    val voltage: Double = 0.0,
    val current: Double = 0.0,
    val power: Double = 0.0,          // branch total, watts
    val energyKwh: Double = 0.0,      // cumulative
    val powerFactor: Double = 0.0,
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val motion: Boolean = false,
    val idleMinutes: Int = 0,         // since the last motion event
    val online: Boolean = false,
    val updatedAt: Long = 0L
)

/**
 * The waste verdict, mirroring devices/{id}/waste.
 *
 * The decision is made once on the ESP32, which holds the authoritative measurement
 * and timing, and is stored rather than recalculated by each client — so every client
 * that reads it sees the same verdict.
 */
data class WasteStatus(
    val detected: Boolean = false,
    val totalPower: Double = 0.0,
    val idleMinutes: Int = 0,
    val channelsOn: List<String> = emptyList()   // channel ids, e.g. ["ch1", "ch4"]
)

/** Settings under users/{uid} that the ESP32 reads on start-up. */
data class UserSettings(
    val electricityRatePhp: Double = 12.50,
    val idleThresholdMinutes: Int = 10,
    val wastePowerThresholdW: Int = 25,
    val highDrawLimitW: Int = 2000
)