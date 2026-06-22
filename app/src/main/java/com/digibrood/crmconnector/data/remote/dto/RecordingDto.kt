package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/**
 * POST /recordings/presign request body. The CRM requires the "device_id" so it
 * can authorise and attribute the upload. `phone` is included so the CRM can
 * attach the recording by number when the recording id and call id differ.
 */
data class PresignRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "file_name") val fileName: String,
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "file_size") val fileSize: Long
)

/** POST /recordings/presign response body (R2 presigned upload). */
data class PresignResponse(
    @Json(name = "upload_url") val uploadUrl: String?,
    @Json(name = "method") val method: String? = "PUT",
    @Json(name = "object_key") val objectKey: String? = null,
    @Json(name = "recording_id") val recordingId: String? = null,
    @Json(name = "headers") val headers: Map<String, String>? = null
)

/** POST /recordings/confirm request body. Includes phone as a safety net so the
 * CRM can attach the recording by number even if ids differ. */
data class ConfirmRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "recording_id") val recordingId: String? = null,
    @Json(name = "object_key") val objectKey: String? = null,
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "file_size") val fileSize: Long,
    @Json(name = "success") val success: Boolean = true
)

/** POST /recordings/confirm response body. Success indicated by "ok":true. */
data class ConfirmResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "message") val message: String? = null
)

/** GET /recordings/trace response — lets the app verify a call's recording state. */
data class RecordingTraceResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "client_call_id") val clientCallId: String? = null,
    @Json(name = "call_synced") val callSynced: Boolean = false,
    @Json(name = "call") val call: TraceCall? = null,
    @Json(name = "recording_held_pending") val recordingHeldPending: Boolean = false,
    @Json(name = "hint") val hint: String? = null
)

data class TraceCall(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "has_recording") val hasRecording: Int? = null,
    @Json(name = "recording_attached") val recordingAttached: Boolean? = null,
    @Json(name = "ai_status") val aiStatus: String? = null,
    @Json(name = "synced_at") val syncedAt: String? = null
)
