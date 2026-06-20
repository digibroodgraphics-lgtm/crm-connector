package com.digibrood.crmconnector.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Date/time helpers for parsing server timestamps and computing day boundaries. */
object TimeUtils {

    private val fallbackFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss"
    )

    /**
     * Parses a server timestamp to epoch millis. Handles:
     *  - epoch seconds/millis as a numeric string,
     *  - ISO-8601 with offset (e.g. 2026-06-19T13:06:28+00:00) via java.time,
     *  - "Z" UTC form, plain local date-times, and a space-separated form.
     * Returns 0 if it cannot be parsed.
     */
    fun parseToEpochMillis(value: String?): Long {
        if (value.isNullOrBlank()) return 0L
        val raw = value.trim()

        // Numeric epoch (seconds or millis).
        raw.toLongOrNull()?.let { num ->
            return if (num < 100_000_000_000L) num * 1000L else num
        }

        // Preferred: java.time ISO parsing (robust for offsets like +00:00).
        runCatching { return OffsetDateTime.parse(raw).toInstant().toEpochMilli() }
        runCatching { return Instant.parse(raw).toEpochMilli() }
        runCatching {
            return LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .toInstant(ZoneOffset.UTC).toEpochMilli()
        }

        // Fallback: explicit SimpleDateFormat patterns (non-lenient).
        for (pattern in fallbackFormats) {
            runCatching {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    if (pattern.endsWith("'Z'")) timeZone = TimeZone.getTimeZone("UTC")
                }
                return sdf.parse(raw)?.time ?: return@runCatching
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

    /** Formats epoch millis as an ISO-8601 string with timezone offset (CRM format). */
    fun toIso8601(epochMillis: Long): String =
        OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

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
