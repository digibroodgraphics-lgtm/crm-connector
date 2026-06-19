package com.digibrood.crmconnector.util

/** Lightweight phone-number validation and normalisation. */
object PhoneUtils {

    /** Keeps a leading + and digits only. */
    fun normalize(input: String?): String {
        val raw = input?.trim().orEmpty()
        if (raw.isEmpty()) return ""
        val hasPlus = raw.startsWith("+")
        val digits = raw.filter { it.isDigit() }
        return if (hasPlus) "+$digits" else digits
    }

    /** Basic validity check: 7-15 digits, optional leading +. */
    fun isValid(input: String?): Boolean {
        val normalized = normalize(input)
        val digits = normalized.removePrefix("+")
        return digits.length in 7..15 && digits.all { it.isDigit() }
    }

    /** A safe display value for unknown / private numbers. */
    fun displayOrUnknown(number: String?): String =
        number?.takeIf { it.isNotBlank() } ?: "Unknown"
}
