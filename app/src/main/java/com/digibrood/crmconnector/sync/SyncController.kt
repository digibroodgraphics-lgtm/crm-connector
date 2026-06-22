package com.digibrood.crmconnector.sync

import com.digibrood.crmconnector.data.repository.CallRepository
import com.digibrood.crmconnector.data.repository.ContactRepository
import com.digibrood.crmconnector.data.repository.DeviceRepository
import com.digibrood.crmconnector.data.repository.MetaRepository
import com.digibrood.crmconnector.data.repository.RecordingRepository
import com.digibrood.crmconnector.data.repository.SettingsRepository
import com.digibrood.crmconnector.data.repository.WhitelistRepository
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.util.ConnectivityObserver
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates one complete sync cycle. Shared by the periodic worker, the
 * one-time worker triggered after a call, and the foreground service.
 *
 * Order of operations:
 *  1. Refresh device status (so revocation takes effect immediately).
 *  2. Only continue if APPROVED (activation-forward, no work otherwise).
 *  3. Refresh settings (popup toggle, recording path, interval).
 *  4. Capture new calls, sync the call queue, upload recordings, flush remarks.
 */
@Singleton
class SyncController @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val settingsRepository: SettingsRepository,
    private val callRepository: CallRepository,
    private val recordingRepository: RecordingRepository,
    private val contactRepository: ContactRepository,
    private val metaRepository: MetaRepository,
    private val whitelistRepository: WhitelistRepository,
    private val connectivity: ConnectivityObserver
) {

    /** @return true when the cycle completed cleanly; false signals a retry. */
    suspend fun runFullSync(): Boolean {
        if (!connectivity.isOnline()) return false

        // Always refresh status first so denied/revoked/inactive halts work.
        deviceRepository.refreshStatus()
        val status = deviceRepository.currentStatus()
        if (status != DeviceStatus.APPROVED) {
            // Not approved: nothing to sync, but this is not a failure to retry.
            return true
        }

        settingsRepository.refreshSettings()
        metaRepository.refresh()
        // Best-effort: re-send any whitelist proposals that never reached the CRM.
        whitelistRepository.retryUnproposed()

        callRepository.captureNewCalls()
        callRepository.backfillRecordings()
        val callsOk = callRepository.syncPending()
        val recordingsOk = recordingRepository.uploadPending()
        contactRepository.syncPendingRemarks()

        return callsOk && recordingsOk
    }

    /** Uploads only the recording queue (used by the dedicated upload worker). */
    suspend fun runRecordingUpload(): Boolean {
        if (!connectivity.isOnline()) return false
        if (deviceRepository.currentStatus() != DeviceStatus.APPROVED) return true
        // Backfill first so recordings written after the call are picked up.
        callRepository.backfillRecordings()
        val callsOk = callRepository.syncPending()
        val recordingsOk = recordingRepository.uploadPending()
        return callsOk && recordingsOk
    }

    /** Sends a heartbeat and refreshes the cached device status. */
    suspend fun runHeartbeat(): Boolean {
        if (!connectivity.isOnline()) return false
        deviceRepository.heartbeat()
        return true
    }
}
