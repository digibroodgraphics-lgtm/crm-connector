package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/**
 * GET /contacts/lookup response body. The CRM returns the matched contact's
 * display name in a field named "name" and its id in "contact_id".
 */
data class ContactLookupResponse(
    @Json(name = "found") val found: Boolean = false,
    @Json(name = "name") val contactName: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "contact_id") val contactId: String? = null
)

/**
 * POST /calls/remark request body. The CRM expects the number in "phone" and the
 * originating "device_id".
 */
data class RemarkRequest(
    @Json(name = "device_id") val deviceId: String? = null,
    @Json(name = "client_call_id") val clientCallId: String? = null,
    @Json(name = "call_id") val callId: String? = null,
    @Json(name = "phone") val phone: String,
    @Json(name = "contact_name") val contactName: String? = null,
    @Json(name = "remark") val remark: String,
    @Json(name = "status") val status: String? = null
)

/** POST /calls/remark response body. Success is indicated by "ok":true. */
data class RemarkResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "contact_id") val contactId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "message") val message: String? = null
)
