package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.local.dao.CallDao
import com.digibrood.crmconnector.data.local.entity.CallEntity
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.CallSyncRequest
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.domain.model.CallType
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.util.CallLogReader
import com.digibrood.crmconnector.util.CapturedCall
import com.digibrood.crmconnector.util.DeviceInfoProvider
import com.digibrood.crmconnector.util.TimeUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** A stable reference to a captured call, shared with the after-call popup. */
data class CapturedCallRef(
    val clientCallId: String,
    val callType: String,
    val phone: String
)

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
    private val deviceInfo: DeviceInfoProvider,
    private val recordingRepository: RecordingRepository
) {

    fun pendingCountFlow(): Flow<Int> = callDao.pendingCount()

    suspend fun syncedTodayCount(): Int = callDao.syncedSince(TimeUtils.startOfTodayMillis())

    suspend fun lastSyncedCall(): CallEntity? = withContext(Dispatchers.IO) { callDao.lastSyncedCall() }

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
            .filter { it.startTime > activatedAt && it.phoneNumber.isNotBlank() }

        var inserted = 0
        for (captured in newCalls) {
            // Skip calls already captured (e.g. by the real-time receiver path).
            if (callDao.findByNumberAndStart(captured.phoneNumber, captured.startTime) != null) continue
            val clientCallId = UUID.randomUUID().toString()
            val hasRecording = recordingRepository.discoverForCall(
                clientCallId = clientCallId,
                phone = captured.phoneNumber,
                callStart = captured.startTime,
                callEnd = captured.endTime
            )
            val rowId = callDao.insert(captured.toEntity(clientCallId, hasRecording))
            if (rowId != -1L) inserted++
        }
        inserted
    }

    /**
     * Captures the just-ended call in real time (called from the call receiver),
     * returning a stable reference so the after-call popup can send the SAME
     * client_call_id and call_type to /calls/remark. Returns null if the call is
     * not yet in the system log, is before activation, or has no phone number.
     *
     * @param callStartedAtApprox the approximate wall-clock time the call began.
     */
    suspend fun captureRecentCall(callStartedAtApprox: Long): CapturedCallRef? = withContext(Dispatchers.IO) {
        val status = DeviceStatus.fromApi(prefs.deviceStatus)
        val activatedAt = prefs.activatedAtEpochMs
        if (status != DeviceStatus.APPROVED || activatedAt <= 0L) return@withContext null

        val recent = callLogReader.readMostRecentCall() ?: return@withContext null
        // Not the just-ended call yet (log entry not written) — let the caller retry.
        if (recent.startTime < callStartedAtApprox - 60_000L) return@withContext null
        if (recent.startTime <= activatedAt) return@withContext null
        if (recent.phoneNumber.isBlank()) return@withContext null

        val existing = callDao.findByNumberAndStart(recent.phoneNumber, recent.startTime)
        if (existing != null) {
            return@withContext CapturedCallRef(
                clientCallId = existing.clientCallId,
                callType = CallType.fromApi(existing.callType).apiValue,
                phone = existing.phoneNumber
            )
        }

        val clientCallId = UUID.randomUUID().toString()
        val hasRecording = recordingRepository.discoverForCall(
            clientCallId, recent.phoneNumber, recent.startTime, recent.endTime
        )
        callDao.insert(recent.toEntity(clientCallId, hasRecording))
        CapturedCallRef(clientCallId, recent.callType.apiValue, recent.phoneNumber)
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
            clientCallId, captured.phoneNumber, captured.startTime, captured.endTime
        )
        val rowId = callDao.insert(captured.toEntity(clientCallId, hasRecording))
        if (rowId != -1L) clientCallId else null
    }

    /**
     * Syncs all pending calls to the CRM — ONE call per request (the format the
     * CRM accepts). A call is marked synced when the CRM stores it; a per-call
     * "rejected" result is treated as terminal (so it doesn't clog the queue) but
     * its reason is surfaced. Transport/auth failures stop the run for a retry.
     */
    suspend fun syncPending(batchSize: Int = 100): Boolean = withContext(Dispatchers.IO) {
        val pending = (callDao.getByState(CallEntity.SyncState.PENDING, batchSize) +
            callDao.getByState(CallEntity.SyncState.FAILED, batchSize))
            .distinctBy { it.clientCallId }
        if (pending.isEmpty()) return@withContext true

        var allOk = true
        var accepted = 0
        var rejected = 0
        var lastReason: String? = null

        for (call in pending) {
            val now = System.currentTimeMillis()
            callDao.markState(listOf(call.clientCallId), CallEntity.SyncState.SYNCING, now)

            when (val result = safeApiCall(moshi) { api.syncCalls(call.toSyncRequest(deviceInfo.deviceId)) }) {
                is NetworkResult.Success -> {
                    val r = result.data.results.firstOrNull()
                    val status = r?.status
                    val isRejected = status.equals("rejected", true) || status.equals("error", true)
                    // Either way it's terminal — mark synced so it leaves the queue.
                    callDao.markSynced(call.clientCallId, r?.callId)
                    prefs.lastSyncEpochMs = now
                    if (isRejected) {
                        rejected++
                        lastReason = r?.reason ?: status
                    } else {
                        accepted++
                    }
                }

                is NetworkResult.ApiFailure -> {
                    callDao.markState(listOf(call.clientCallId), CallEntity.SyncState.FAILED, now)
                    prefs.lastSyncResult =
                        "API rejected: code=${result.errorCode ?: "?"} http=${result.httpCode}"
                    allOk = false
                    break
                }

                is NetworkResult.NetworkError -> {
                    callDao.markState(listOf(call.clientCallId), CallEntity.SyncState.FAILED, now)
                    prefs.lastSyncResult = "Network error: ${result.throwable.message ?: "unknown"}"
                    allOk = false
                    break
                }
            }
        }

        if (accepted > 0 || rejected > 0) {
            prefs.lastSyncResult = if (rejected == 0) {
                "OK: $accepted call(s) accepted"
            } else {
                "OK: $accepted accepted, $rejected rejected (${lastReason ?: "unknown"})"
            }
        }
        allOk
    }

    /**
     * Re-syncs a call with the fields collected from the after-call popup
     * (note/status/tags/stage). Uses the SAME client_call_id so the CRM updates
     * the existing call/contact rather than creating a duplicate.
     */
    suspend fun applyPopupFields(
        clientCallId: String,
        note: String?,
        status: String?,
        tags: List<Int>?,
        stageId: Int?
    ): Boolean = withContext(Dispatchers.IO) {
        val call = callDao.getByClientId(clientCallId) ?: return@withContext false
        val result = safeApiCall(moshi) {
            api.syncCalls(call.toSyncRequest(deviceInfo.deviceId, note, status, tags, stageId))
        }
        result is NetworkResult.Success
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

    private fun CallEntity.toSyncRequest(
        deviceId: String,
        note: String? = null,
        status: String? = null,
        tags: List<Int>? = null,
        stageId: Int? = null
    ): CallSyncRequest {
        val type = CallType.fromApi(callType).apiValue
        val startIso = TimeUtils.toIso8601(startTime)
        val endIso = TimeUtils.toIso8601(endTime)
        return CallSyncRequest(
            deviceId = deviceId,
            clientCallId = clientCallId,
            phone = phoneNumber,
            number = phoneNumber,
            callType = type,
            direction = type,
            startTime = startIso,
            startedAt = startIso,
            endTime = endIso,
            endedAt = endIso,
            duration = duration,
            hasRecording = hasRecording,
            note = note,
            status = status,
            tags = tags,
            stageId = stageId
        )
    }
}
