package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/**
 * GET /settings response body. Field names follow the DIGIBROOD CRM contract.
 * Unknown extra fields (capture rules, business hours, etc.) are ignored.
 */
data class SettingsResponse(
    @Json(name = "call_popup_enabled") val callPopupEnabled: Boolean? = null,
    @Json(name = "recording_path") val recordingPath: String? = null,
    @Json(name = "recording_upload") val recordingUpload: Boolean? = null,
    @Json(name = "auto_sync") val autoSync: Boolean? = null,
    @Json(name = "heartbeat_interval_sec") val heartbeatIntervalSec: Long? = null,
    @Json(name = "settings_version") val settingsVersion: Int? = null
)

/** GET /branding response body. */
data class BrandingResponse(
    @Json(name = "logo_url") val logoUrl: String? = null,
    @Json(name = "icon_url") val iconUrl: String? = null,
    @Json(name = "app_name") val appName: String? = null
)

/** GET /stats response body. */
data class StatsResponse(
    @Json(name = "connection_status") val connectionStatus: String? = null,
    @Json(name = "calls_today") val callsSyncedToday: Int = 0,
    @Json(name = "uploads_today") val recordingsUploadedToday: Int = 0,
    @Json(name = "last_sync") val lastSyncTime: String? = null
)
