package com.example.safehome.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safehome.R
import com.example.safehome.model.Device
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * Renders the four fixed relay channels.
 *
 * There is no remove action: the channels are fixed in the wiring and cannot be
 * deleted, only renamed and switched.
 */
class DeviceAdapter(
    private val devices: MutableList<Device>,
    private val onToggle: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    /** Channel ids currently implicated in a waste event, so those rows can be marked. */
    private var wastingIds: Set<String> = emptySet()

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvChannelBadge: TextView = itemView.findViewById(R.id.tvChannelBadge)
        val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        val tvDeviceStatus: TextView = itemView.findViewById(R.id.tvDeviceStatus)
        val swChannel: SwitchMaterial = itemView.findViewById(R.id.swChannel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        val context = holder.itemView.context

        holder.tvChannelBadge.text = device.channelLabel
        holder.tvDeviceName.text = device.name.ifEmpty { "Unassigned" }

        val wasting = device.isOn && wastingIds.contains(device.id)
        holder.tvDeviceStatus.text = when {
            wasting -> context.getString(R.string.channel_status_wasting)
            device.isOn -> context.getString(R.string.channel_status_on)
            else -> context.getString(R.string.channel_status_off)
        }
        holder.tvDeviceStatus.setTextColor(
            context.getColor(if (wasting) R.color.alert else R.color.muted)
        )

        // Detach the listener before setting the state. Without this, binding a
        // recycled row fires onToggle and sends a command the user never issued.
        holder.swChannel.setOnCheckedChangeListener(null)
        holder.swChannel.isChecked = device.isOn
        holder.swChannel.setOnCheckedChangeListener { _, _ -> onToggle(device) }
    }

    fun updateDevices(newDevices: List<Device>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    fun setWastingChannels(ids: Set<String>) {
        wastingIds = ids
        notifyDataSetChanged()
    }
}