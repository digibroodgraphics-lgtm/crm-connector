package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/**
 * POST /calls/sync request body — ONE call per request, fields at the top level
 * (this is the format the DIGIBROOD CRM accepts). phone in E.164, call_type,
 * ISO-8601 timestamps with offset, duration in seconds, has_recording, and the
 * stable client_call_id.
 */
data class CallSyncRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "call_type") val callType: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String,
    @Json(name = "duration") val duration: Long,
    @Json(name = "has_recording") val hasRecording: Boolean
)

/** Per-call result returned by the CRM. status "stored" (or similar) = success. */
data class CallSyncResult(
    @Json(name = "client_call_id") val clientCallId: String? = null,
    @Json(name = "call_id") val callId: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "contact_id") val contactId: String? = null
)

/** POST /calls/sync response body. */
data class CallSyncResponse(
    @Json(name = "ok") val ok: Boolean = true,
    @Json(name = "results") val results: List<CallSyncResult> = emptyList()
)
