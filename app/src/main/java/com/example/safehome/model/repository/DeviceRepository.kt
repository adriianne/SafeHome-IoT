package com.example.safehome.model.repository

import com.example.safehome.model.Device
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Repository for Device operations
 * Handles Firebase Realtime Database or Firestore for device management
 */
class DeviceRepository {
    
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val devicesCollection = firestore.collection("devices")
    
    /**
     * Get current user ID
     */
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Get all devices for current user
     */
    suspend fun getAllDevices(): List<Device> {
        val userId = getCurrentUserId() ?: return emptyList()
        
        return try {
            val snapshot = devicesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Device::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Add a new device
     */
    suspend fun addDevice(device: Device): Result<Unit> {
        val userId = getCurrentUserId()
        if (userId == null) {
            return Result.failure(Exception("User not logged in"))
        }
        
        return try {
            val deviceWithUser = device.copy(id = devicesCollection.document().id)
            // Note: In production, you'd want to store userId in the device document
            devicesCollection.document(deviceWithUser.id).set(deviceWithUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update device status
     */
    suspend fun updateDevice(device: Device): Result<Unit> {
        return try {
            devicesCollection.document(device.id).set(device).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove a device
     */
    suspend fun removeDevice(deviceId: String): Result<Unit> {
        return try {
            devicesCollection.document(deviceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle device power state
     */
    suspend fun toggleDevice(deviceId: String, isOn: Boolean): Result<Unit> {
        return try {
            devicesCollection.document(deviceId)
                .update("isOn", isOn)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
