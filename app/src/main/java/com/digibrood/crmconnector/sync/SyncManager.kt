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
        // Starting a foreground service can throw on Android 12+ if the app is not
        // in an allowed state; never let that crash the app.
        runCatching { SyncForegroundService.start(context) }
        runCatching { scheduler.startAll() }
    }

    fun stopSync() {
        runCatching { scheduler.cancelAll() }
        runCatching { SyncForegroundService.stop(context) }
    }
}
