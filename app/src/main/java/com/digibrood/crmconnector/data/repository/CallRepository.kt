package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.local.dao.CallDao
import com.digibrood.crmconnector.data.local.entity.CallEntity
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.CallSyncItem
import com.digibrood.crmconnector.data.remote.dto.CallSyncRequest
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.domain.model.CallType
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.util.CallLogReader
import com.digibrood.crmconnector.util.CapturedCall
import com.digibrood.crmconnector.util.TimeUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the call queue. Captures new calls from the system log (activation-forward
 * only), generates a client_call_id, attaches any recording, and syncs queued
 * calls to the CRM in batches with per-call result handling.
 */
@Singleton
class CallRepository @Inject constructor(
    private val api: CrmApiService,
    private val moshi: Moshi,
    private val callDao: CallDao,
    private val prefs: SecurePrefs,
    private val callLogReader: CallLogReader,
    private val recordingRepository: RecordingRepository
) {

    fun pendingCountFlow(): Flow<Int> = callDao.pendingCount()

    suspend fun syncedTodayCount(): Int = callDao.syncedSince(TimeUtils.startOfTodayMillis())

    /**
     * Captures calls that occurred after the device's activation time and after
     * the last captured call. Returns the number of new calls queued.
     *
     * Activation-forward only: never imports historical calls. If the device is
     * not approved or has no activation time, nothing is captured.
     */
    suspend fun captureNewCalls(): Int = withContext(Dispatchers.IO) {
        val status = DeviceStatus.fromApi(prefs.deviceStatus)
        val activatedAt = prefs.activatedAtEpochMs
        if (status != DeviceStatus.APPROVED || activatedAt <= 0L) return@withContext 0

        val watermark = maxOf(activatedAt, callDao.latestCapturedStartTime() ?: 0L)
        val newCalls = callLogReader.readCallsSince(watermark)
            .filter { it.startTime > activatedAt }

        var inserted = 0
        for (captured in newCalls) {
            val clientCallId = UUID.randomUUID().toString()
            val hasRecording = recordingRepository.discoverForCall(
                clientCallId = clientCallId,
                callStart = captured.startTime,
                callEnd = captured.endTime
            )
            val rowId = callDao.insert(captured.toEntity(clientCallId, hasRecording))
            if (rowId != -1L) inserted++
        }
        inserted
    }

    /**
     * Enqueues a single call captured in real time (e.g. from the call receiver).
     * Returns the generated client_call_id, or null if it was outside activation.
     */
    suspend fun enqueueCaptured(captured: CapturedCall): String? = withContext(Dispatchers.IO) {
        val activatedAt = prefs.activatedAtEpochMs
        if (activatedAt <= 0L || captured.startTime <= activatedAt) return@withContext null

        val clientCallId = UUID.randomUUID().toString()
        val hasRecording = recordingRepository.discoverForCall(
            clientCallId, captured.startTime, captured.endTime
        )
        val rowId = callDao.insert(captured.toEntity(clientCallId, hasRecording))
        if (rowId != -1L) clientCallId else null
    }

    /**
     * Syncs all pending calls to the CRM. Returns true if every queued call was
     * acknowledged (or there was nothing to sync), false if a retry is needed.
     */
    suspend fun syncPending(batchSize: Int = 50): Boolean = withContext(Dispatchers.IO) {
        var allOk = true
        while (true) {
            val pending = callDao.getByState(CallEntity.SyncState.PENDING, batchSize) +
                callDao.getByState(CallEntity.SyncState.FAILED, batchSize)
            val batch = pending.distinctBy { it.clientCallId }.take(batchSize)
            if (batch.isEmpty()) break

            val now = System.currentTimeMillis()
            callDao.markState(batch.map { it.clientCallId }, CallEntity.SyncState.SYNCING, now)

            val request = CallSyncRequest(calls = batch.map { it.toSyncItem() })
            when (val result = safeApiCall(moshi) { api.syncCalls(request) }) {
                is NetworkResult.Success -> {
                    val body = result.data
                    val acknowledged = (body.synced + body.results.map { it.clientCallId }).toSet()
                    body.results.forEach { r ->
                        callDao.markSynced(r.clientCallId, r.callId)
                    }
                    batch.filter { it.clientCallId in acknowledged }.forEach {
                        callDao.markSynced(it.clientCallId, null)
                    }
                    val notAcked = batch.filter { it.clientCallId !in acknowledged }
                    if (notAcked.isNotEmpty()) {
                        callDao.markState(notAcked.map { it.clientCallId }, CallEntity.SyncState.PENDING, now)
                    }
                    prefs.lastSyncEpochMs = now
                    if (batch.size < batchSize) break
                }

                is NetworkResult.ApiFailure, is NetworkResult.NetworkError -> {
                    callDao.markState(batch.map { it.clientCallId }, CallEntity.SyncState.FAILED, now)
                    allOk = false
                    break
                }
            }
        }
        allOk
    }

    private fun CapturedCall.toEntity(clientCallId: String, hasRecording: Boolean) = CallEntity(
        clientCallId = clientCallId,
        phoneNumber = phoneNumber,
        startTime = startTime,
        endTime = endTime,
        duration = durationSeconds,
        callType = callType.apiValue,
        hasRecording = hasRecording
    )

    private fun CallEntity.toSyncItem() = CallSyncItem(
        clientCallId = clientCallId,
        phoneNumber = phoneNumber,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        callType = CallType.fromApi(callType).apiValue,
        hasRecording = hasRecording
    )
}
