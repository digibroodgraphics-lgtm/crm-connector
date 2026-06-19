package com.digibrood.crmconnector.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Date/time helpers for parsing server timestamps and computing day boundaries. */
object TimeUtils {

    private val isoFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss"
    )

    /**
     * Parses a server timestamp (ISO-8601 variants or epoch millis/seconds as a
     * string) to epoch millis. Returns 0 if it cannot be parsed.
     */
    fun parseToEpochMillis(value: String?): Long {
        if (value.isNullOrBlank()) return 0L

        // Numeric epoch (seconds or millis).
        value.toLongOrNull()?.let { num ->
            return if (num < 100_000_000_000L) num * 1000L else num
        }

        for (pattern in isoFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                if (pattern.endsWith("'Z'")) sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(value)?.time ?: continue
            } catch (_: Exception) {
                // try next pattern
            }
        }
        return 0L
    }

    /** Formats epoch millis as a short human-readable local time. Returns null for 0. */
    fun formatReadable(epochMillis: Long): String? {
        if (epochMillis <= 0L) return null
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    /** Epoch millis for the start of the current local day (00:00). */
    fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
