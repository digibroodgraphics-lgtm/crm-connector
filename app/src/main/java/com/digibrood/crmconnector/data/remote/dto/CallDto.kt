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

/** POST /calls/sync request body. */
data class CallSyncRequest(
    @Json(name = "calls") val calls: List<CallSyncItem>
)

/** Per-call result returned by the CRM for a sync batch. */
data class CallSyncResult(
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "call_id") val callId: String? = null,
    @Json(name = "status") val status: String? = null
)

/** POST /calls/sync response body. */
data class CallSyncResponse(
    @Json(name = "results") val results: List<CallSyncResult> = emptyList(),
    @Json(name = "synced") val synced: List<String> = emptyList()
)
