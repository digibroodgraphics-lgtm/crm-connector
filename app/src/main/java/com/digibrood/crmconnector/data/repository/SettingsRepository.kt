package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.SettingsResponse
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.util.Constants
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls CRM-controlled settings (popup toggle, recording path override, sync
 * interval, max upload size) and caches the relevant ones in secure prefs.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val api: CrmApiService,
    private val moshi: Moshi,
    private val prefs: SecurePrefs
) {

    @Volatile
    var syncIntervalMinutes: Long = Constants.DEFAULT_SYNC_INTERVAL_MINUTES
        private set

    suspend fun refreshSettings(): NetworkResult<SettingsResponse> {
        val result = safeApiCall(moshi) { api.getSettings() }
        if (result is NetworkResult.Success) {
            val body = result.data
            body.callPopupEnabled?.let { prefs.callPopupEnabled = it }
            body.recordingPath?.let { prefs.recordingPathOverride = it }
            body.syncIntervalMinutes?.takeIf { it > 0 }?.let { syncIntervalMinutes = it }
        }
        return result
    }

    val callPopupEnabled: Boolean get() = prefs.callPopupEnabled
}
