package com.digibrood.crmconnector.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A phone number the user has proposed to be whitelisted (e.g. a personal
 * number) so its calls/recordings are NOT uploaded to the CRM.
 *
 * Whitelisting requires admin approval: a proposed number stays [Status.PENDING]
 * and its calls keep uploading until the CRM admin approves it
 * ([Status.APPROVED]) in CRM -> Call Connector -> Devices -> Whitelist. Only
 * APPROVED numbers are excluded from upload. The approval state is refreshed from
 * the `whitelist` array on GET /device/status.
 */
@Entity(
    tableName = "whitelist",
    indices = [Index(value = ["number"], unique = true)]
)
data class WhitelistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Normalised phone number (E.164-ish: leading + and digits). */
    val number: String,
    /** Optional user-supplied label (e.g. "My personal phone"). */
    val label: String? = null,
    /** PENDING, APPROVED, REJECTED. */
    val status: String = Status.PENDING,
    /** True once POST /whitelist/propose has been acknowledged by the CRM. */
    val proposedToCrm: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    object Status {
        const val PENDING = "PENDING"
        const val APPROVED = "APPROVED"
        const val REJECTED = "REJECTED"
    }
}
