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

/**
 * One entry in the `whitelist` array on GET /device/status (D7).
 *
 * The CRM contract for this array has changed between releases — it has been
 * delivered both as plain E.164 strings (every entry implicitly approved) and as
 * objects `{number, status}`. [WhitelistItemAdapter] parses BOTH shapes into this
 * model so the app is resilient to either, treating a bare string as approved.
 */
data class WhitelistItem(
    val number: String? = null,
    val status: String? = null
)
