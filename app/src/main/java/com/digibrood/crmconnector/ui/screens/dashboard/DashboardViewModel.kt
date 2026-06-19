package com.digibrood.crmconnector.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.repository.CallRepository
import com.digibrood.crmconnector.data.repository.DeviceRepository
import com.digibrood.crmconnector.data.repository.RecordingRepository
import com.digibrood.crmconnector.data.repository.SettingsRepository
import com.digibrood.crmconnector.data.repository.StatsRepository
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.sync.SyncController
import com.digibrood.crmconnector.sync.SyncManager
import com.digibrood.crmconnector.util.ConnectivityObserver
import com.digibrood.crmconnector.util.CallLogReader
import com.digibrood.crmconnector.util.CrashReporter
import com.digibrood.crmconnector.util.DeviceInfoProvider
import com.digibrood.crmconnector.util.PermissionManager
import com.digibrood.crmconnector.util.TimeUtils
import android.Manifest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val status: DeviceStatus = DeviceStatus.UNKNOWN,
    val registeredNumber: String? = null,
    val online: Boolean = false,
    val callsToday: Int = 0,
    val recordingsToday: Int = 0,
    val lastSync: String? = null,
    val pendingCalls: Int = 0,
    val pendingRecordings: Int = 0,
    val refreshing: Boolean = false,
    // ---- Diagnostics (to pinpoint why calls may not capture) ----
    val diagCallLogPermission: Boolean = false,
    val diagActivation: String = "-",
    val diagCallsVisible: Int = 0,
    val diagCallsAfterActivation: Int = 0,
    val diagLatestCall: String = "-",
    val diagLastCrash: String? = null,
    val diagDeviceId: String = "-",
    val diagLastSyncResult: String = "-"
) {
    val pendingTotal: Int get() = pendingCalls + pendingRecordings
}

/**
 * Backs the dashboard. Live counts (pending queue, connectivity) come from
 * reactive flows; status/stats are refreshed from the CRM on demand and on
 * first load. Applies the sync state whenever the device status changes.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository,
    private val callRepository: CallRepository,
    private val recordingRepository: RecordingRepository,
    private val connectivity: ConnectivityObserver,
    private val syncManager: SyncManager,
    private val syncController: SyncController,
    private val permissionManager: PermissionManager,
    private val callLogReader: CallLogReader,
    private val crashReporter: CrashReporter,
    private val deviceInfo: DeviceInfoProvider,
    private val prefs: SecurePrefs
) : ViewModel() {

    private val _state = MutableStateFlow(
        DashboardUiState(
            status = deviceRepository.currentStatus(),
            registeredNumber = prefs.registeredNumber,
            online = connectivity.isOnline(),
            lastSync = TimeUtils.formatReadable(prefs.lastSyncEpochMs)
        )
    )
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        combine(
            callRepository.pendingCountFlow(),
            recordingRepository.pendingCountFlow(),
            connectivity.observe()
        ) { pendingCalls, pendingRecordings, online ->
            Triple(pendingCalls, pendingRecordings, online)
        }.onEach { (pendingCalls, pendingRecordings, online) ->
            _state.update {
                it.copy(
                    pendingCalls = pendingCalls,
                    pendingRecordings = pendingRecordings,
                    online = online
                )
            }
        }.launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true, diagLastCrash = crashReporter.lastCrashSummary()) }

            // First make sure we have the latest status and settings.
            runCatching { deviceRepository.refreshStatus() }
            runCatching { settingsRepository.refreshSettings() }

            val status = deviceRepository.currentStatus()
            syncManager.applyStatus(status)
            deviceRepository.ensureActivatedIfApproved()

            // If approved, immediately capture any new calls and push the queue so
            // tapping Refresh gives instant results (rather than waiting for the
            // background worker).
            if (status == DeviceStatus.APPROVED) {
                runCatching { syncController.runFullSync() }
            }

            val remote = runCatching { statsRepository.fetchRemoteStats() }.getOrNull()
            val local = statsRepository.localStats()

            val callsToday: Int
            val recordingsToday: Int
            if (remote is NetworkResult.Success) {
                callsToday = maxOf(remote.data.callsSyncedToday, local.callsSyncedToday)
                recordingsToday = maxOf(remote.data.recordingsUploadedToday, local.recordingsUploadedToday)
            } else {
                callsToday = local.callsSyncedToday
                recordingsToday = local.recordingsUploadedToday
            }

            // ---- Diagnostics ----
            val hasCallLog = permissionManager.isGranted(Manifest.permission.READ_CALL_LOG)
            val activatedAt = prefs.activatedAtEpochMs
            val visibleCalls = if (hasCallLog) callLogReader.readCallsSince(0L, limit = 500) else emptyList()
            val afterActivation = visibleCalls.count { it.startTime > activatedAt }
            val latestCall = visibleCalls.maxByOrNull { it.startTime }?.startTime ?: 0L

            _state.update {
                it.copy(
                    status = status,
                    registeredNumber = prefs.registeredNumber,
                    callsToday = callsToday,
                    recordingsToday = recordingsToday,
                    lastSync = TimeUtils.formatReadable(prefs.lastSyncEpochMs),
                    online = connectivity.isOnline(),
                    refreshing = false,
                    diagCallLogPermission = hasCallLog,
                    diagActivation = if (activatedAt > 0L) (TimeUtils.formatReadable(activatedAt) ?: "-") else "not set",
                    diagCallsVisible = visibleCalls.size,
                    diagCallsAfterActivation = afterActivation,
                    diagLatestCall = TimeUtils.formatReadable(latestCall) ?: "-",
                    diagLastCrash = crashReporter.lastCrashSummary(),
                    diagDeviceId = deviceInfo.deviceId,
                    diagLastSyncResult = prefs.lastSyncResult ?: "-"
                )
            }
        }
    }
}
