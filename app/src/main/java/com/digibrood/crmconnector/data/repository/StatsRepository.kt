package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.local.dao.CallDao
import com.digibrood.crmconnector.data.local.dao.RecordingDao
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.StatsResponse
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.util.TimeUtils
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

/** Aggregated dashboard figures, with a local fallback when offline. */
data class DashboardStats(
    val callsSyncedToday: Int,
    val recordingsUploadedToday: Int,
    val lastSyncEpochMs: Long
)

@Singleton
class StatsRepository @Inject constructor(
    private val api: CrmApiService,
    private val moshi: Moshi,
    private val callDao: CallDao,
    private val recordingDao: RecordingDao,
    private val prefs: SecurePrefs
) {

    /** Computes today's figures from the local queue (always available). */
    suspend fun localStats(): DashboardStats {
        val startOfDay = TimeUtils.startOfTodayMillis()
        return DashboardStats(
            callsSyncedToday = callDao.syncedSince(startOfDay),
            recordingsUploadedToday = recordingDao.uploadedSince(startOfDay),
            lastSyncEpochMs = prefs.lastSyncEpochMs
        )
    }

    /** Fetches authoritative figures from the CRM; ignored on failure. */
    suspend fun fetchRemoteStats(): NetworkResult<StatsResponse> =
        safeApiCall(moshi) { api.getStats() }
}
