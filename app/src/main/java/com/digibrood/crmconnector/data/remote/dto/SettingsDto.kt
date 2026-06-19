package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/** GET /settings response body. */
data class SettingsResponse(
    @Json(name = "call_popup_enabled") val callPopupEnabled: Boolean? = null,
    @Json(name = "recording_path") val recordingPath: String? = null,
    @Json(name = "scan_paths") val scanPaths: List<String>? = null,
    @Json(name = "sync_interval_minutes") val syncIntervalMinutes: Long? = null,
    @Json(name = "max_recording_mb") val maxRecordingMb: Long? = null
)

/** GET /branding response body. */
data class BrandingResponse(
    @Json(name = "logo_url") val logoUrl: String? = null,
    @Json(name = "app_name") val appName: String? = null,
    @Json(name = "primary_color") val primaryColor: String? = null
)

/** GET /stats response body. */
data class StatsResponse(
    @Json(name = "calls_synced_today") val callsSyncedToday: Int = 0,
    @Json(name = "recordings_uploaded_today") val recordingsUploadedToday: Int = 0,
    @Json(name = "last_sync_time") val lastSyncTime: String? = null,
    @Json(name = "pending_count") val pendingCount: Int = 0
)
