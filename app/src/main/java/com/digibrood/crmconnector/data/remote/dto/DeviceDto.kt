package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/** POST /device/register request body. */
data class RegisterDeviceRequest(
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "device_name") val deviceName: String,
    @Json(name = "device_model") val deviceModel: String,
    @Json(name = "os_version") val osVersion: String,
    @Json(name = "app_version") val appVersion: String
)

/** POST /device/register response body. */
data class RegisterDeviceResponse(
    @Json(name = "device_status") val deviceStatus: String?,
    @Json(name = "registered_number") val registeredNumber: String? = null,
    @Json(name = "activated_at") val activatedAt: String? = null,
    @Json(name = "message") val message: String? = null
)

/** POST /device/change-number request body. */
data class ChangeNumberRequest(
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "device_id") val deviceId: String
)

/** GET /device/status response body. */
data class DeviceStatusResponse(
    @Json(name = "device_status") val deviceStatus: String?,
    @Json(name = "registered_number") val registeredNumber: String? = null,
    @Json(name = "activated_at") val activatedAt: String? = null,
    @Json(name = "call_popup_enabled") val callPopupEnabled: Boolean? = null,
    @Json(name = "message") val message: String? = null
)

/** POST /heartbeat request body. */
data class HeartbeatRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "app_version") val appVersion: String,
    @Json(name = "network_type") val networkType: String? = null,
    @Json(name = "battery_level") val batteryLevel: Int? = null
)

/** POST /heartbeat response body. */
data class HeartbeatResponse(
    @Json(name = "device_status") val deviceStatus: String?,
    @Json(name = "server_time") val serverTime: String? = null
)
