package com.digibrood.crmconnector.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A remark captured from the after-call popup, queued so it can be delivered to
 * the CRM even if the device is offline at the moment the user saves it.
 */
@Entity(
    tableName = "remarks",
    indices = [Index(value = ["syncState"])]
)
data class RemarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientCallId: String?,
    val phoneNumber: String,
    val contactName: String?,
    val company: String?,
    val remark: String,
    val status: String?,
    val syncState: String = SyncState.PENDING,
    val attemptCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    object SyncState {
        const val PENDING = "PENDING"
        const val SYNCED = "SYNCED"
        const val FAILED = "FAILED"
    }
}
