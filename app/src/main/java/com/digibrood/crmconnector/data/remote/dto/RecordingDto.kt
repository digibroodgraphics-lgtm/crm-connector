package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/** POST /recordings/presign request body. */
data class PresignRequest(
    @Json(name = "client_call_id") val clientCallId: String,
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

/** POST /recordings/confirm request body. */
data class ConfirmRequest(
    @Json(name = "recording_id") val recordingId: String? = null,
    @Json(name = "object_key") val objectKey: String? = null,
    @Json(name = "client_call_id") val clientCallId: String,
    @Json(name = "file_size") val fileSize: Long,
    @Json(name = "success") val success: Boolean = true
)

/** POST /recordings/confirm response body. */
data class ConfirmResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "message") val message: String? = null
)
