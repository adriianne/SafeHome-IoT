package com.example.safehome.model.repository

import android.util.Log
import com.example.safehome.model.Device
import com.example.safehome.model.UserSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DeviceRepository {

    private val database = FirebaseDatabase
        .getInstance("https://safehome-6c4db-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference
    private val auth = FirebaseAuth.getInstance()

    private val activeListeners = mutableListOf<Pair<String, ValueEventListener>>()

    companion object {
        const val DEFAULT_DEVICE_ID = "node"
        private const val TAG = "DeviceRepository"
    }

    // ═══════════════════════════════════════════════════════
    // HOME SCREEN — LISTEN FOR DEVICES
    // ═══════════════════════════════════════════════════════
    fun listenForDevices(onUpdate: (List<Device>) -> Unit): ValueEventListener {
        Log.d(TAG, ">>> listenForDevices CALLED")
        Log.d(TAG, ">>> Auth user: ${auth.currentUser?.uid ?: "NOT LOGGED IN"}")
        Log.d(TAG, ">>> Database URL: ${database.toString()}")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, ">>> onDataChange FIRED, children=${snapshot.childrenCount}")
                Log.d(TAG, ">>> Snapshot key: ${snapshot.key}, value: ${snapshot.value}")

                val devices = mutableListOf<Device>()
                for (child in snapshot.children) {
                    val id = child.key ?: continue
                    val name = child.child("name").getValue(String::class.java) ?: "Channel"
                    val isOn = child.child("isOn").getValue(Boolean::class.java) ?: false

                    Log.d(TAG, ">>> Parsed channel: id=$id name=$name isOn=$isOn")

                    devices.add(
                        Device(
                            id = id,
                            name = name,
                            isOn = isOn
                        )
                    )
                }
                devices.sortBy { it.id }
                Log.d(TAG, ">>> Total devices: ${devices.size}")
                onUpdate(devices)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, ">>> onCancelled: code=${error.code} message=${error.message}")
                Log.e(TAG, ">>> Details: ${error.details}")
                onUpdate(emptyList())
            }
        }

        database.child("devices").addValueEventListener(listener)
        Log.d(TAG, ">>> Listener ATTACHED to /devices")
        activeListeners.add("devices" to listener)
        return listener
    }

    // ═══════════════════════════════════════════════════════
    // SEND COMMAND TO ESP32
    // ═══════════════════════════════════════════════════════
    fun sendCommand(deviceId: String, command: String): Boolean {
        return try {
            database.child("devices").child(deviceId).child("command").setValue(command)
            Log.d(TAG, "Command sent: $command → $deviceId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "sendCommand failed: ${e.message}")
            false
        }
    }

    // ═══════════════════════════════════════════════════════
    // SETTINGS
    // ═══════════════════════════════════════════════════════
    suspend fun readSettingsOnce(): UserSettings = suspendCancellableCoroutine { cont ->
        val uid = auth.currentUser?.uid
        if (uid == null) {
            cont.resume(UserSettings())
            return@suspendCancellableCoroutine
        }

        database.child("users").child(uid).child("settings")
            .get()
            .addOnSuccessListener { snapshot ->
                val settings = snapshot.getValue(UserSettings::class.java) ?: UserSettings()
                Log.d(TAG, "Loaded settings: $settings")
                cont.resume(settings)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load settings: ${e.message}")
                cont.resume(UserSettings())
            }
    }

    suspend fun saveSettings(settings: UserSettings): Boolean = suspendCancellableCoroutine { cont ->
        val uid = auth.currentUser?.uid
        if (uid == null) {
            cont.resumeWithException(Exception("Not logged in"))
            return@suspendCancellableCoroutine
        }

        val map = mapOf(
            "electricityRatePhp" to settings.electricityRatePhp,
            "idleThresholdMinutes" to settings.idleThresholdMinutes,
            "wastePowerThresholdW" to settings.wastePowerThresholdW,
            "highDrawLimitW" to settings.highDrawLimitW
        )

        database.child("users").child(uid).child("settings")
            .setValue(map)
            .addOnSuccessListener {
                Log.d(TAG, "Settings saved")
                cont.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save settings: ${e.message}")
                cont.resumeWithException(e)
            }
    }

    fun removeListener() {
        activeListeners.forEach { (path, listener) ->
            database.child(path).removeEventListener(listener)
        }
        activeListeners.clear()
    }

    fun stopAllListeners() {
        removeListener()
    }
}