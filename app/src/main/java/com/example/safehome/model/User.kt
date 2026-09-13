package com.example.safehome.model

import com.example.safehome.utils.DateUtils

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val displayName: String = "",
    val createdAt: Long = 0L
) {
    val createdAtReadable: String
        get() = DateUtils.formatTimestamp(createdAt)

    val createdAtRelative: String
        get() = DateUtils.formatRelativeTime(createdAt)
}