package com.example.safehome.model.repository

import com.example.safehome.model.Device
import com.example.safehome.model.Telemetry
import com.example.safehome.model.UserSettings
import com.example.safehome.model.WasteStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

/**
 * Reads and writes the SafeHome node under devices/{deviceId}, and the account
 * settings under users/{uid}.
 *
 * Every path string in the app lives here. If the schema changes, only this file changes.
 */
class DeviceRepository(
    private val deviceId: String = DEFAULT_DEVICE_ID
) {

    companion object {
        // Single-node prototype. Replace with the id stored under the user's
        // device_ids when the app supports more than one installation.
        const val DEFAULT_DEVICE_ID = "safehome_01"
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    private val uid: String? get() = auth.currentUser?.uid
    private val deviceRef: DatabaseReference get() = db.child("devices").child(deviceId)

    // Held so every listener can be detached in stopAllListeners().
    private val attached = mutableListOf<Pair<DatabaseReference, ValueEventListener>>()

    private fun attach(
        ref: DatabaseReference,
        onData: (DataSnapshot) -> Unit,
        onError: (String) -> Unit
    ) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) = onData(snapshot)
            override fun onCancelled(error: DatabaseError) = onError(error.message)
        }
        ref.addValueEventListener(listener)
        attached += ref to listener
    }

    fun listenToTelemetry(onChange: (Telemetry) -> Unit, onError: (String) -> Unit) {
        attach(deviceRef.child("telemetry"), { snap ->
            snap.getValue(Telemetry::class.java)?.let(onChange)
        }, onError)
    }

    fun listenToWaste(onChange: (WasteStatus) -> Unit, onError: (String) -> Unit) {
        attach(deviceRef.child("waste"), { snap ->
            val detected = snap.child("detected").getValue(Boolean::class.java) ?: false
            val total = snap.child("total_power").getValue(Double::class.java) ?: 0.0
            val idle = snap.child("idle_minutes").getValue(Int::class.java) ?: 0
            // channels_on is stored as { "ch1": true, "ch4": true }
            val on = snap.child("channels_on").children
                .filter { it.getValue(Boolean::class.java) == true }
                .mapNotNull { it.key }
            onChange(WasteStatus(detected, total, idle, on))
        }, onError)
    }

    fun listenToChannels(onChange: (List<Device>) -> Unit, onError: (String) -> Unit) {
        attach(deviceRef.child("appliances"), { snap ->
            val list = snap.children.map { child ->
                Device(
                    id = child.key ?: "",
                    name = child.child("name").getValue(String::class.java) ?: "",
                    isOn = child.child("state").getValue(String::class.java) == "ON"
                )
            }.sortedBy { it.id }
            onChange(list)
        }, onError)
    }

    fun listenToSettings(onChange: (UserSettings) -> Unit, onError: (String) -> Unit) {
        val currentUid = uid ?: return
        attach(db.child("users").child(currentUid), { snap ->
            onChange(
                UserSettings(
                    electricityRatePhp = snap.child("electricityRatePhp")
                        .getValue(Double::class.java) ?: 12.50,
                    idleThresholdMinutes = snap.child("idleThresholdMinutes")
                        .getValue(Int::class.java) ?: 10,
                    wastePowerThresholdW = snap.child("wastePowerThresholdW")
                        .getValue(Int::class.java) ?: 25,
                    highDrawLimitW = snap.child("highDrawLimitW")
                        .getValue(Int::class.java) ?: 2000
                )
            )
        }, onError)
    }

    /**
     * Writes a command for the node to execute. The app never writes appliance state
     * directly — only the node does, after the relay has physically switched.
     *
     * @param target channel id such as "ch1", or null for ALL_OFF
     * @param action one of ON, OFF, ALL_OFF (validated again by the security rules)
     */
    suspend fun sendCommand(target: String?, action: String) {
        val currentUid = uid ?: throw IllegalStateException("No signed-in user")
        val command = mapOf(
            "target" to (target ?: ""),
            "action" to action,
            "requested_by" to currentUid,
            "requested_at" to System.currentTimeMillis(),
            "acknowledged" to false
        )
        deviceRef.child("commands").setValue(command).await()
    }

    /** Reads the account settings once, for prefilling a form. */
    suspend fun readSettingsOnce(): UserSettings {
        val currentUid = uid ?: throw IllegalStateException("No signed-in user")
        val snap = db.child("users").child(currentUid).get().await()
        return UserSettings(
            electricityRatePhp = snap.child("electricityRatePhp")
                .getValue(Double::class.java) ?: 12.50,
            idleThresholdMinutes = snap.child("idleThresholdMinutes")
                .getValue(Int::class.java) ?: 10,
            wastePowerThresholdW = snap.child("wastePowerThresholdW")
                .getValue(Int::class.java) ?: 25,
            highDrawLimitW = snap.child("highDrawLimitW")
                .getValue(Int::class.java) ?: 2000
        )
    }

    /**
     * Writes only the four threshold fields.
     *
     * updateChildren rather than setValue: setValue on users/{uid} would replace the
     * whole record and delete firstName, lastName and email.
     */
    suspend fun saveSettings(settings: UserSettings) {
        val currentUid = uid ?: throw IllegalStateException("No signed-in user")
        val updates = mapOf<String, Any>(
            "electricityRatePhp" to settings.electricityRatePhp,
            "idleThresholdMinutes" to settings.idleThresholdMinutes,
            "wastePowerThresholdW" to settings.wastePowerThresholdW,
            "highDrawLimitW" to settings.highDrawLimitW
        )
        db.child("users").child(currentUid).updateChildren(updates).await()
    }

    /** Must be called from the view's onDestroy, or the listeners leak. */
    fun stopAllListeners() {
        attached.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        attached.clear()
    }
}