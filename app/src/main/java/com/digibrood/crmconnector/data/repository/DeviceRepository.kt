package com.digibrood.crmconnector.data.repository

import android.content.Context
import android.os.BatteryManager
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.ChangeNumberRequest
import com.digibrood.crmconnector.data.remote.dto.DeviceStatusResponse
import com.digibrood.crmconnector.data.remote.dto.HeartbeatRequest
import com.digibrood.crmconnector.data.remote.dto.HeartbeatResponse
import com.digibrood.crmconnector.data.remote.dto.RegisterDeviceRequest
import com.digibrood.crmconnector.data.remote.dto.RegisterDeviceResponse
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.util.ConnectivityObserver
import com.digibrood.crmconnector.util.DeviceInfoProvider
import com.digibrood.crmconnector.util.PhoneUtils
import com.digibrood.crmconnector.util.TimeUtils
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Device registration, number changes, status polling and heartbeats.
 * Keeps [SecurePrefs] in sync with the latest backend-reported device status,
 * registered number and activation time.
 */
@Singleton
class DeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CrmApiService,
    private val moshi: Moshi,
    private val prefs: SecurePrefs,
    private val deviceInfo: DeviceInfoProvider,
    private val connectivity: ConnectivityObserver,
    private val whitelistRepository: WhitelistRepository
) {

    fun currentStatus(): DeviceStatus = DeviceStatus.fromApi(prefs.deviceStatus)

    suspend fun register(phoneNumber: String): NetworkResult<RegisterDeviceResponse> {
        val request = RegisterDeviceRequest(
            phoneNumber = PhoneUtils.normalize(phoneNumber),
            deviceId = deviceInfo.deviceId,
            deviceName = deviceInfo.deviceName,
            deviceModel = deviceInfo.deviceModel,
            osVersion = deviceInfo.osVersion,
            appVersion = deviceInfo.appVersion
        )
        val result = safeApiCall(moshi) { api.registerDevice(request) }
        if (result is NetworkResult.Success) {
            applyRegistration(result.data, fallbackNumber = request.phoneNumber)
        }
        return result
    }

    suspend fun changeNumber(phoneNumber: String): NetworkResult<RegisterDeviceResponse> {
        val request = ChangeNumberRequest(
            phoneNumber = PhoneUtils.normalize(phoneNumber),
            deviceId = deviceInfo.deviceId
        )
        val result = safeApiCall(moshi) { api.changeNumber(request) }
        if (result is NetworkResult.Success) {
            applyRegistration(result.data, fallbackNumber = request.phoneNumber)
        }
        return result
    }

    suspend fun refreshStatus(): NetworkResult<DeviceStatusResponse> {
        val result = safeApiCall(moshi) { api.getDeviceStatus(deviceInfo.deviceId) }
        if (result is NetworkResult.Success) {
            val body = result.data
            body.deviceStatus?.let { prefs.deviceStatus = it }
            body.registeredNumber?.let { prefs.registeredNumber = it }
            body.callPopupEnabled?.let { prefs.callPopupEnabled = it }
            if (!body.activatedAt.isNullOrBlank()) {
                prefs.activatedAtEpochMs = TimeUtils.parseToEpochMillis(body.activatedAt)
            }
            // Reflect the admin's whitelist decisions (D7) when present.
            whitelistRepository.syncFromDeviceStatus(body.whitelist)
            ensureActivationTimestamp()
        }
        return result
    }

    suspend fun heartbeat(): NetworkResult<HeartbeatResponse> {
        val request = HeartbeatRequest(
            deviceId = deviceInfo.deviceId,
            appVersion = deviceInfo.appVersion,
            networkType = if (connectivity.isOnline()) "online" else "offline",
            batteryLevel = readBatteryLevel()
        )
        val result = safeApiCall(moshi) { api.heartbeat(request) }
        if (result is NetworkResult.Success) {
            result.data.deviceStatus?.let { prefs.deviceStatus = it }
            ensureActivationTimestamp()
        }
        return result
    }

    private fun applyRegistration(data: RegisterDeviceResponse, fallbackNumber: String) {
        data.deviceStatus?.let { prefs.deviceStatus = it }
        prefs.registeredNumber = data.registeredNumber ?: fallbackNumber
        if (!data.activatedAt.isNullOrBlank()) {
            prefs.activatedAtEpochMs = TimeUtils.parseToEpochMillis(data.activatedAt)
        }
        ensureActivationTimestamp()
    }

    /**
     * Activation-forward guarantee: the first time the device is seen as APPROVED
     * and no activation time is recorded yet, stamp it with "now". This ensures
     * historical calls are never imported even if the backend omits activated_at.
     */
    private fun ensureActivationTimestamp() {
        if (currentStatus() == DeviceStatus.APPROVED && prefs.activatedAtEpochMs <= 0L) {
            prefs.activatedAtEpochMs = System.currentTimeMillis()
        }
    }

    /** Public hook so callers can guarantee activation is set when approved. */
    fun ensureActivatedIfApproved() = ensureActivationTimestamp()

    private fun readBatteryLevel(): Int? = try {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 }
    } catch (_: Exception) {
        null
    }
}
