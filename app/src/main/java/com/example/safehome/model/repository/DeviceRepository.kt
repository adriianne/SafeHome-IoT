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
        const val DEVICE_ID = "ESP32_001"
        const val DEFAULT_DEVICE_ID = "ESP32_001"
        private const val TAG = "DeviceRepository"
    }

    // ═══════════════════════════════════════════════════════
    // LISTEN FOR 4 CHANNELS (real-time)
    // ═══════════════════════════════════════════════════════
    fun listenForDevices(onUpdate: (List<Device>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = mutableListOf<Device>()
                for (node in snapshot.children) {
                    val channelsSnap = node.child("channels")
                    for (channel in channelsSnap.children) {
                        val chId = channel.key ?: continue
                        val name = channel.child("name").getValue(String::class.java) ?: "Channel"
                        val isOn = channel.child("isOn").getValue(Boolean::class.java) ?: false

                        devices.add(
                            Device(id = chId, name = name, isOn = isOn)
                        )
                    }
                }
                devices.sortBy { it.id }
                Log.d(TAG, "Received ${devices.size} channels")
                onUpdate(devices)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Listener cancelled: ${error.message}")
                onUpdate(emptyList())
            }
        }

        database.child("devices").addValueEventListener(listener)
        activeListeners.add("devices" to listener)
        return listener
    }

    // ═══════════════════════════════════════════════════════
    // SEND COMMAND TO ONE CHANNEL
    // ═══════════════════════════════════════════════════════
    fun sendCommand(channelId: String, command: String): Boolean {
        return try {
            database.child("devices")
                .child(DEVICE_ID)
                .child("channels")
                .child(channelId)
                .child("command")
                .setValue(command)
            Log.d(TAG, "Command sent: $command → $channelId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "sendCommand failed: ${e.message}")
            false
        }
    }

    // ═══════════════════════════════════════════════════════
    // TURN ALL OFF
    // ═══════════════════════════════════════════════════════
    fun turnAllOff(): Boolean {
        return try {
            val updates = mapOf(
                "ch1/command" to "OFF",
                "ch2/command" to "OFF",
                "ch3/command" to "OFF",
                "ch4/command" to "OFF"
            )
            database.child("devices").child(DEVICE_ID).child("channels")
                .updateChildren(updates)
            Log.d(TAG, "All-OFF sent to 4 channels")
            true
        } catch (e: Exception) {
            Log.e(TAG, "turnAllOff failed: ${e.message}")
            false
        }
    }

    // ═══════════════════════════════════════════════════════
    // SETTINGS (existing methods)
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
                cont.resume(snapshot.getValue(UserSettings::class.java) ?: UserSettings())
            }
            .addOnFailureListener { cont.resume(UserSettings()) }
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
            .addOnSuccessListener { cont.resume(true) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    fun removeListener() {
        activeListeners.forEach { (path, listener) ->
            database.child(path).removeEventListener(listener)
        }
        activeListeners.clear()
    }

    fun stopAllListeners() = removeListener()
}