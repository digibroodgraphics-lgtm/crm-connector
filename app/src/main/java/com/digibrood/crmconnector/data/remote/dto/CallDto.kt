package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/**
 * POST /calls/sync request body — ONE call per request. Sends a superset of field
 * names for maximum CRM compatibility: both phone/number, call_type/direction and
 * start_time/started_at (+end). Plus optional note/status/tags/stage_id which the
 * CRM applies to the matched/created contact.
 */
data class CallSyncRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "number") val number: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "call_type") val callType: String,
    @Json(name = "direction") val direction: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "started_at") val startedAt: String,
    @Json(name = "end_time") val endTime: String,
    @Json(name = "ended_at") val endedAt: String,
    @Json(name = "duration") val duration: Long,
    @Json(name = "has_recording") val hasRecording: Boolean,
    @Json(name = "platform") val platform: String? = null,
    @Json(name = "note") val note: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "tags") val tags: List<Int>? = null,
    @Json(name = "stage_id") val stageId: Int? = null
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
