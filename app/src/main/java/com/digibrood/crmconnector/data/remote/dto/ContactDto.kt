package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/** GET /contacts/lookup response body. */
data class ContactLookupResponse(
    @Json(name = "found") val found: Boolean = false,
    @Json(name = "contact_name") val contactName: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "contact_id") val contactId: String? = null
)

/** POST /calls/remark request body. */
data class RemarkRequest(
    @Json(name = "client_call_id") val clientCallId: String? = null,
    @Json(name = "call_id") val callId: String? = null,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "contact_name") val contactName: String? = null,
    @Json(name = "remark") val remark: String,
    @Json(name = "status") val status: String? = null
)

/** POST /calls/remark response body. */
data class RemarkResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "message") val message: String? = null
)
