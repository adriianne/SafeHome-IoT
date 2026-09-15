package com.example.safehome.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    /**
     * Converts a Unix timestamp (milliseconds) to a readable date string.
     * Example: 1788413175943 -> "Sep 11, 2026 12:34 PM"
     */
    fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0) return "N/A"
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Short version without time.
     * Example: 1788413175943 -> "Sep 11, 2026"
     */
    fun formatDateOnly(timestamp: Long): String {
        if (timestamp <= 0) return "N/A"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Relative time — "2 minutes ago", "1 hour ago", "3 days ago"
     */
    fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0) return "N/A"
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000} minutes ago"
            diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
            diff < 604_800_000 -> "${diff / 86_400_000} days ago"
            else -> formatDateOnly(timestamp)
        }
    }
}