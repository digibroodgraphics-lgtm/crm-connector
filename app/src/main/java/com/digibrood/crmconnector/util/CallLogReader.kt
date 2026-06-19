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
     * first. Empty if the permission is not granted.
     */
    fun readCallsSince(sinceMillis: Long, limit: Int = 200): List<CapturedCall> {
        if (!hasPermission()) return emptyList()

        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )
        val selection = "${CallLog.Calls.DATE} > ?"
        val selectionArgs = arrayOf(sinceMillis.toString())
        val sortOrder = "${CallLog.Calls.DATE} ASC LIMIT $limit"

        val results = mutableListOf<CapturedCall>()
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val numberIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val dateIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val durationIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val typeIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIdx) ?: continue
                val date = cursor.getLong(dateIdx)
                val durationSec = cursor.getLong(durationIdx)
                val type = cursor.getInt(typeIdx)
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
        return results
    }

    /** Returns the single most recent call, or null. */
    fun readMostRecentCall(): CapturedCall? {
        if (!hasPermission()) return null
        return readCallsSince(0L, limit = 1).maxByOrNull { it.startTime }
            ?: readAll(limit = 1).firstOrNull()
    }

    private fun readAll(limit: Int): List<CapturedCall> {
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )
        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit"
        val results = mutableListOf<CapturedCall>()
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI, projection, null, null, sortOrder
        )?.use { cursor ->
            val numberIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val dateIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val durationIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val typeIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIdx) ?: continue
                val date = cursor.getLong(dateIdx)
                val durationSec = cursor.getLong(durationIdx)
                val type = cursor.getInt(typeIdx)
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
        return results
    }
}
