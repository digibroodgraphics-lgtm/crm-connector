package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/**
 * POST /whitelist/propose request body (D7, LIVE).
 *
 * Sends a number the user wants whitelisted to the CRM admin for approval. The
 * CRM keeps it pending until an admin approves it per-device under
 * CRM -> Call Connector -> Devices -> Whitelist. Until approval, that number's
 * calls keep uploading as normal.
 */
data class WhitelistProposeRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "number") val number: String,
    @Json(name = "note") val note: String? = null
)

/** POST /whitelist/propose response body. `status` is pending|approved. */
data class WhitelistProposeResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "id") val id: Long? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null
)
