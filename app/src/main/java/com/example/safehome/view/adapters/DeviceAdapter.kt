package com.example.safehome.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.safehome.R
import com.example.safehome.model.Device
import com.google.android.material.switchmaterial.SwitchMaterial

class DeviceAdapter(
    private var devices: List<Device>,
    private val onToggleClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    fun updateDevices(newDevices: List<Device>) {
        devices = newDevices
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvChannelBadge: TextView = itemView.findViewById(R.id.tvChannelBadge)
        private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        private val tvDeviceStatus: TextView = itemView.findViewById(R.id.tvDeviceStatus)
        private val swChannel: SwitchMaterial = itemView.findViewById(R.id.swChannel)

        fun bind(device: Device) {
            tvChannelBadge.text = device.channelLabel
            tvDeviceName.text = device.name
            tvDeviceStatus.text = if (device.isOn) "On" else "Off"
            tvDeviceStatus.setTextColor(
                ContextCompat.getColor(itemView.context, if (device.isOn) R.color.ok else R.color.muted)
            )

            // Detach listener first (prevents recycled-view bugs)
            swChannel.setOnCheckedChangeListener(null)
            swChannel.isChecked = device.isOn
            swChannel.setOnCheckedChangeListener { _, _ ->
                onToggleClick(device)
            }
        }
    }
}