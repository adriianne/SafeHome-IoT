package com.example.safehome.utils

object Validators {

    private val NAME_REGEX = Regex("^[\\p{L} .'-]{2,40}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val MIN_PASSWORD_LENGTH = 8

    fun validateName(value: String, fieldLabel: String): String? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> "Enter your ${fieldLabel.lowercase()}"
            trimmed.length < 2 -> "$fieldLabel is too short"
            !NAME_REGEX.matches(trimmed) -> "Use letters only"
            else -> null
        }
    }

    fun validateEmail(value: String): String? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> "Enter your email"
            !EMAIL_REGEX.matches(trimmed) -> "That doesn't look like an email address"
            else -> null
        }
    }

    fun validateNewPassword(value: String): String? = when {
        value.isEmpty() -> "Choose a password"
        value.length < MIN_PASSWORD_LENGTH -> "Use at least $MIN_PASSWORD_LENGTH characters"
        value.none { it.isDigit() } -> "Include at least one number"
        value.none { it.isLetter() } -> "Include at least one letter"
        else -> null
    }

    fun validateExistingPassword(value: String): String? =
        if (value.isEmpty()) "Enter your password" else null

    fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Re-enter your password"
        confirm != password -> "Passwords don't match"
        else -> null
    }
}