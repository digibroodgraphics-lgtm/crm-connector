package com.digibrood.crmconnector.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.sync.SyncManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Restarts background sync after the device reboots, but only if the device is
 * already approved. This keeps the foreground service and scheduled work alive
 * across restarts without any user interaction.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: SecurePrefs
    @Inject lateinit var syncManager: SyncManager

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val status = DeviceStatus.fromApi(prefs.deviceStatus)
            if (status == DeviceStatus.APPROVED) {
                syncManager.applyStatus(status)
            }
        }
    }
}
