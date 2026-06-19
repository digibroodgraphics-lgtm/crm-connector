package com.digibrood.crmconnector.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A discovered call-recording file queued for upload to R2 via the presign flow.
 * Linked to a call by [clientCallId].
 */
@Entity(
    tableName = "recordings",
    indices = [
        Index(value = ["filePath"], unique = true),
        Index(value = ["uploadState"]),
        Index(value = ["clientCallId"])
    ]
)
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientCallId: String,
    val filePath: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val recordedAt: Long,
    /** PENDING, UPLOADING, UPLOADED, FAILED, SKIPPED (e.g. too large) */
    val uploadState: String = UploadState.PENDING,
    val attemptCount: Int = 0,
    val lastAttemptAt: Long = 0L,
    val recordingId: String? = null,
    val objectKey: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    object UploadState {
        const val PENDING = "PENDING"
        const val UPLOADING = "UPLOADING"
        const val UPLOADED = "UPLOADED"
        const val FAILED = "FAILED"
        const val SKIPPED = "SKIPPED"
    }
}
