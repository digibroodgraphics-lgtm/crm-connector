package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/** A single call record inside the POST /calls/sync payload. */
data class CallSyncItem(
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "start_time") val startTime: Long,
    @Json(name = "end_time") val endTime: Long,
    @Json(name = "duration") val duration: Long,
    @Json(name = "call_type") val callType: String,
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

/** Per-call result optionally returned by the CRM for a sync batch. */
data class CallSyncResult(
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "call_id") val callId: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "reason") val reason: String? = null
)

/**
 * POST /calls/sync response body. The CRM returns "ok":true on success; the
 * detailed per-call fields are optional, so a 2xx response is treated as the
 * whole batch having been accepted.
 */
data class CallSyncResponse(
    @Json(name = "ok") val ok: Boolean = true,
    @Json(name = "results") val results: List<CallSyncResult> = emptyList(),
    @Json(name = "synced") val synced: List<String> = emptyList()
)
