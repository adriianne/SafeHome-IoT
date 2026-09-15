package com.example.safehome.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safehome.R
import com.example.safehome.model.Device

class DeviceAdapter(
    private var devices: List<Device>,
    private val onToggleClick: (Device) -> Unit,
    private val onRemoveClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.bind(device)
    }

    override fun getItemCount(): Int = devices.size

    fun updateDevices(newDevices: List<Device>) {
        devices = newDevices
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        private val tvDeviceStatus: TextView = itemView.findViewById(R.id.tvDeviceStatus)
        private val tvPowerUsage: TextView = itemView.findViewById(R.id.tvPowerUsage)
        private val btnToggle: Button = itemView.findViewById(R.id.btnToggle)
        private val btnRemove: Button = itemView.findViewById(R.id.btnRemove)

        fun bind(device: Device) {
            tvDeviceName.text = device.name
            tvDeviceStatus.text = if (device.isConnected) "🟢 Connected" else "🔴 Disconnected"
            tvDeviceStatus.setTextColor(
                if (device.isConnected) itemView.context.getColor(R.color.green)
                else itemView.context.getColor(R.color.red)
            )
            tvPowerUsage.text = "${device.powerUsage}W"
            btnToggle.text = if (device.isOn) "Turn Off" else "Turn On"
            btnToggle.setBackgroundColor(
                if (device.isOn) itemView.context.getColor(R.color.red)
                else itemView.context.getColor(R.color.green)
            )

            btnToggle.setOnClickListener { onToggleClick(device) }
            btnRemove.setOnClickListener { onRemoveClick(device) }
        }
    }
}