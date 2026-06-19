package com.digibrood.crmconnector.sync

import android.content.Context
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.service.SyncForegroundService
import com.digibrood.crmconnector.util.PermissionManager
import com.digibrood.crmconnector.worker.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single decision point that turns background sync on or off based on the
 * device's approval status. Approved + permissions granted starts the foreground
 * service and schedules work; any other status stops everything.
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduler: SyncScheduler,
    private val permissionManager: PermissionManager
) {

    /** Applies the given status: starts or stops all background work accordingly. */
    fun applyStatus(status: DeviceStatus) {
        when {
            status.isActive && permissionManager.areCorePermissionsGranted() -> startSync()
            else -> stopSync()
        }
    }

    fun startSync() {
        if (!permissionManager.areCorePermissionsGranted()) return
        SyncForegroundService.start(context)
        scheduler.startAll()
    }

    fun stopSync() {
        scheduler.cancelAll()
        SyncForegroundService.stop(context)
    }
}
