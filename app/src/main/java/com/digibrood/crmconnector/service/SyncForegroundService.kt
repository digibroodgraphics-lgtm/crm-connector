package com.digibrood.crmconnector.service

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.digibrood.crmconnector.data.repository.DeviceRepository
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.sync.SyncController
import com.digibrood.crmconnector.util.ConnectivityObserver
import com.digibrood.crmconnector.util.Constants
import com.digibrood.crmconnector.worker.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Persistent foreground service that keeps the CRM connection alive.
 *
 * Responsibilities:
 *  - Show the ongoing "CRM Connector active" notification (data-sync type).
 *  - Run a sync whenever connectivity returns (offline queue flush).
 *  - Stop itself if the device is no longer approved.
 *
 * Periodic and retrying work is delegated to WorkManager via [SyncScheduler];
 * this service guarantees a live process and instant offline-recovery sync.
 */
@AndroidEntryPoint
class SyncForegroundService : LifecycleService() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var syncController: SyncController
    @Inject lateinit var connectivity: ConnectivityObserver
    @Inject lateinit var deviceRepository: DeviceRepository
    @Inject lateinit var scheduler: SyncScheduler

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        observeConnectivity()
        runInitialSync()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startAsForeground()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun startAsForeground() {
        val notification = notificationHelper.buildSyncNotification()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        ServiceCompat.startForeground(this, Constants.SYNC_NOTIFICATION_ID, notification, type)
    }

    private fun observeConnectivity() {
        lifecycleScope.launch {
            connectivity.observe().collect { online ->
                if (online) {
                    runCatching { syncController.runFullSync() }
                    stopIfNotApproved()
                }
            }
        }
    }

    private fun runInitialSync() {
        lifecycleScope.launch {
            runCatching { syncController.runFullSync() }
            stopIfNotApproved()
        }
    }

    private fun stopIfNotApproved() {
        if (deviceRepository.currentStatus() != DeviceStatus.APPROVED) {
            scheduler.cancelAll()
            stopForegroundCompat()
            stopSelf()
        }
    }

    private fun stopForegroundCompat() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SyncForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SyncForegroundService::class.java))
        }
    }
}
