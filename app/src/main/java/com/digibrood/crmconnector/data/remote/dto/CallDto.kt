package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/**
 * A single call record inside the POST /calls/sync payload, matching the CRM
 * contract: phone in E.164, call_type, ISO-8601 timestamps (with offset),
 * duration in seconds, has_recording, and the stable client_call_id.
 */
data class CallSyncItem(
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "call_type") val callType: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String,
    @Json(name = "duration") val duration: Long,
    @Json(name = "has_recording") val hasRecording: Boolean
)

/**
 * POST /calls/sync request body. The CRM requires the "device_id" alongside the
 * batch of calls so it can attribute and de-duplicate them.
 */
data class CallSyncRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "calls") val calls: List<CallSyncItem>
)

/** Per-call result returned by the CRM for a sync batch. */
data class CallSyncResult(
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "call_id") val callId: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "reason") val reason: String? = null
)

/**
 * POST /calls/sync response body. The CRM returns "ok":true with a per-call
 * results array (status + reason).
 */
data class CallSyncResponse(
    @Json(name = "ok") val ok: Boolean = true,
    @Json(name = "results") val results: List<CallSyncResult> = emptyList(),
    @Json(name = "synced") val synced: List<String> = emptyList()
)
