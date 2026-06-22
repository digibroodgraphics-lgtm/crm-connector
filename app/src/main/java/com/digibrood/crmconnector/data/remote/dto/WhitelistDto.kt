package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/**
 * POST /whitelist/propose request body (D7).
 *
 * Sends a number the user wants whitelisted to the CRM admin for approval. The
 * CRM keeps it pending until an admin approves it per-device under
 * CRM -> Call Connector -> Devices -> Whitelist.
 *
 * NOTE: this endpoint is part of the agreed D7 contract but may not be live on
 * the CRM yet. The app proposes optimistically and tolerates a 404/None — the
 * number stays locally PENDING and is re-proposed on the next sync.
 */
data class WhitelistProposeRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "number") val number: String,
    @Json(name = "label") val label: String? = null
)

/** POST /whitelist/propose response body. `status` is pending|approved|rejected. */
data class WhitelistProposeResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null
)

/**
 * One entry in the `whitelist` array on GET /device/status. The app reads this
 * to reflect the admin's approval decision back into the UI and the upload gate.
 */
data class WhitelistItem(
    @Json(name = "number") val number: String? = null,
    @Json(name = "status") val status: String? = null
)
