package com.digibrood.crmconnector.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.digibrood.crmconnector.domain.model.CallType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** A single call read from the system call log. */
data class CapturedCall(
    val phoneNumber: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val callType: CallType
)

/**
 * Reads call entries from the system [CallLog]. Activation-forward only: callers
 * always pass a `since` timestamp so historical calls are never imported.
 *
 * Note: the result count is capped in code rather than via a "LIMIT" clause in
 * the sort order, because some OEM call-log providers (e.g. Samsung) reject
 * "LIMIT" in the sortOrder argument with "IllegalArgumentException: Invalid
 * token LIMIT". The whole read is also wrapped defensively so a provider quirk
 * can never crash the app.
 */
@Singleton
class CallLogReader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Returns calls whose start time is strictly after [sinceMillis], oldest
     * first, capped at [limit]. Empty if the permission is not granted or on any
     * provider error.
     */
    fun readCallsSince(sinceMillis: Long, limit: Int = 200): List<CapturedCall> {
        if (!hasPermission()) return emptyList()
        return query(
            selection = "${CallLog.Calls.DATE} > ?",
            selectionArgs = arrayOf(sinceMillis.toString()),
            descending = false,
            limit = limit
        )
    }

    /** Returns the single most recent call, or null. */
    fun readMostRecentCall(): CapturedCall? {
        if (!hasPermission()) return null
        return query(
            selection = null,
            selectionArgs = null,
            descending = true,
            limit = 1
        ).firstOrNull()
    }

    /**
     * Returns the caller-ID name the system cached for the most recent call to/from
     * [phoneNumber], if any. This is how a caller-ID app such as **Truecaller**
     * surfaces an unknown caller's name: when it identifies a call, the resolved
     * name is written to the call log's CACHED_NAME column. Best-effort — depends
     * on the caller-ID app's integration and the OEM dialer.
     */
    fun cachedNameFor(phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank() || !hasPermission()) return null
        val target = PhoneUtils.normalize(phoneNumber)
        try {
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER),
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                if (nameIdx >= 0 && numIdx >= 0) {
                    var scanned = 0
                    while (cursor.moveToNext() && scanned < 300) {
                        scanned++
                        val num = cursor.getString(numIdx) ?: continue
                        if (PhoneUtils.normalize(num) == target) {
                            val name = cursor.getString(nameIdx)
                            if (!name.isNullOrBlank()) return name
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Provider quirk / revoked permission — fall through to null.
        }
        return null
    }

    private fun query(
        selection: String?,
        selectionArgs: Array<String>?,
        descending: Boolean,
        limit: Int
    ): List<CapturedCall> {
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )
        val results = mutableListOf<CapturedCall>()
        try {
            val cursor = openCursor(projection, selection, selectionArgs, descending, limit)
            cursor?.use {
                val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                if (numberIdx < 0 || dateIdx < 0 || durationIdx < 0 || typeIdx < 0) {
                    return emptyList()
                }
                while (it.moveToNext() && results.size < limit) {
                    val number = it.getString(numberIdx) ?: continue
                    val date = it.getLong(dateIdx)
                    val durationSec = it.getLong(durationIdx)
                    val type = it.getInt(typeIdx)
                    results.add(
                        CapturedCall(
                            phoneNumber = PhoneUtils.normalize(number),
                            startTime = date,
                            endTime = date + durationSec * 1000L,
                            durationSeconds = durationSec,
                            callType = CallType.fromCallLogType(type)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Provider quirk / revoked permission mid-read — return what we have.
        }
        return results
    }

    /**
     * Opens the call-log cursor. On Android 11+ uses the query Bundle
     * (QUERY_ARG_SORT_DIRECTION + QUERY_ARG_LIMIT), which OEM providers (Samsung)
     * honour reliably — unlike a "LIMIT"/sort string in the legacy sortOrder arg,
     * which Samsung either rejects or ignores (previously yielding stale/oldest
     * rows). Falls back to the legacy form on older versions.
     */
    private fun openCursor(
        projection: Array<String>,
        selection: String?,
        selectionArgs: Array<String>?,
        descending: Boolean,
        limit: Int
    ) = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        val args = android.os.Bundle().apply {
            if (selection != null) {
                putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                if (selectionArgs != null) {
                    putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                }
            }
            putStringArray(
                android.content.ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(CallLog.Calls.DATE)
            )
            putInt(
                android.content.ContentResolver.QUERY_ARG_SORT_DIRECTION,
                if (descending) android.content.ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                else android.content.ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
            )
            putInt(android.content.ContentResolver.QUERY_ARG_LIMIT, limit)
        }
        context.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, args, null)
    } else {
        val order = "${CallLog.Calls.DATE} ${if (descending) "DESC" else "ASC"}"
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, order
        )
    }
}
