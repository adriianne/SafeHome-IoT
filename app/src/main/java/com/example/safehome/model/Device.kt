package com.example.safehome.model

/**
 * One relay channel on the SafeHome node.
 *
 * The four channels are fixed by the hardware, so there is no MAC address, no
 * discovery and no per-device connection state — the node is either online or it
 * isn't, and that is reported once in [Telemetry].
 *
 * There is deliberately no powerUsage field. A single PZEM-004T measures the branch
 * total upstream of all four relays, so attributing watts to one channel would be
 * a number the hardware cannot produce.
 */
data class Device(
    val id: String = "",        // "ch1" … "ch4"
    val name: String = "",      // user-assigned, e.g. "Electric Fan"
    val isOn: Boolean = false
) {
    /** "CH1", shown as the badge on each row. */
    val channelLabel: String
        get() = id.uppercase()
}