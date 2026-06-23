package com.digibrood.crmconnector.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A captured call queued for synchronisation with the CRM.
 *
 * [clientCallId] is a locally generated UUID used to de-duplicate on the server
 * and to correlate recording uploads. Rows are inserted by the call receiver /
 * call-log reader and removed (or marked synced) once the CRM acknowledges them.
 */
@Entity(
    tableName = "calls",
    indices = [
        Index(value = ["clientCallId"], unique = true),
        Index(value = ["syncState"]),
        Index(value = ["startTime"])
    ]
)
data class CallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientCallId: String,
    val phoneNumber: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val callType: String,
    val hasRecording: Boolean = false,
    /** Non-null for VoIP/app calls (e.g. "com.whatsapp"); null for normal PSTN calls. */
    val platform: String? = null,
    /** Display name for VoIP calls (which have no phone number). */
    val displayName: String? = null,
    /** PENDING, SYNCING, SYNCED, FAILED */
    val syncState: String = SyncState.PENDING,
    val attemptCount: Int = 0,
    val lastAttemptAt: Long = 0L,
    /** Server-assigned id once synced (used when attaching remarks/recordings). */
    val serverCallId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    object SyncState {
        const val PENDING = "PENDING"
        const val SYNCING = "SYNCING"
        const val SYNCED = "SYNCED"
        const val FAILED = "FAILED"
    }
}
