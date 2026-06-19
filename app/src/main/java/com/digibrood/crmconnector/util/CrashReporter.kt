package com.digibrood.crmconnector.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Captures the last uncaught exception to plain on-device storage so it can be
 * surfaced in the in-app diagnostics panel. This makes remote troubleshooting
 * possible without access to logcat.
 */
@Singleton
class CrashReporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun record(throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val time = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US).format(Date())
            val text = "[$time] ${throwable.javaClass.simpleName}: ${throwable.message}\n$sw"
            prefs.edit().putString(KEY, text.take(4000)).apply()
        } catch (_: Throwable) {
            // Never let crash logging itself crash.
        }
    }

    /** A short one-line summary of the last crash, or null if none. */
    fun lastCrashSummary(): String? {
        val full = prefs.getString(KEY, null) ?: return null
        return full.lineSequence().firstOrNull()?.take(160)
    }

    fun lastCrashFull(): String? = prefs.getString(KEY, null)

    fun clear() = prefs.edit().remove(KEY).apply()

    companion object {
        private const val FILE = "crm_crash_log"
        private const val KEY = "last_crash"
    }
}
