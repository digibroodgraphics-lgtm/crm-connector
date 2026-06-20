package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/** Nested contact object some CRM responses use. */
data class ContactInfo(
    @Json(name = "name") val name: String? = null,
    @Json(name = "company") val company: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "contact_id") val contactId: String? = null
)

/**
 * GET /contacts/lookup response body. The CRM may return the contact fields at
 * the top level or nested under "contact"; both are supported so the popup can
 * pre-fill Name and Company.
 */
data class ContactLookupResponse(
    @Json(name = "found") val found: Boolean = false,
    @Json(name = "name") val name: String? = null,
    @Json(name = "company") val company: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "contact_id") val contactId: String? = null,
    @Json(name = "contact") val contact: ContactInfo? = null
) {
    val effectiveName: String? get() = contact?.name ?: name
    val effectiveCompany: String? get() = contact?.company ?: company
    val effectiveStatus: String? get() = contact?.status ?: status
}

/**
 * POST /calls/remark request body. The CRM expects the number in "phone", plus
 * optional "name" and "company" (applied smartly server-side), the originating
 * "device_id", and the "client_call_id" to link the note to the call.
 */
data class RemarkRequest(
    @Json(name = "device_id") val deviceId: String? = null,
    @Json(name = "client_call_id") val clientCallId: String? = null,
    @Json(name = "phone") val phone: String,
    @Json(name = "call_type") val callType: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "company") val company: String? = null,
    @Json(name = "remark") val remark: String,
    @Json(name = "status") val status: String? = null
)

/** POST /calls/remark response body. Success is indicated by "ok":true. */
data class RemarkResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "contact_id") val contactId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "company") val company: String? = null,
    @Json(name = "message") val message: String? = null
)
