package com.digibrood.crmconnector.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.overlay.CallPopupActivity
import com.digibrood.crmconnector.util.Constants
import com.digibrood.crmconnector.util.PermissionManager
import com.digibrood.crmconnector.util.PhoneUtils
import com.digibrood.crmconnector.worker.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Detects call start/end transitions. When a call ends and the device is
 * approved, it triggers an immediate sync (which captures the call and uploads
 * any recording) and, if enabled, shows the after-call overlay popup.
 *
 * Handles incoming, outgoing, missed and rejected calls — all four resolve to a
 * transition back to IDLE, after which the call log is read by the sync worker.
 */
@AndroidEntryPoint
class CallReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: SyncScheduler
    @Inject lateinit var prefs: SecurePrefs
    @Inject lateinit var permissionManager: PermissionManager

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_NEW_OUTGOING_CALL -> {
                @Suppress("DEPRECATION")
                intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)?.let {
                    lastNumber = PhoneUtils.normalize(it)
                }
            }

            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> handlePhoneState(context, intent)
        }
    }

    private fun handlePhoneState(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        @Suppress("DEPRECATION")
        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)?.let {
            if (it.isNotBlank()) lastNumber = PhoneUtils.normalize(it)
        }

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                wasInCall = true
                lastState = state
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                wasInCall = true
                wentOffHook = true
                lastState = state
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (wasInCall && lastState != TelephonyManager.EXTRA_STATE_IDLE) {
                    onCallEnded(context, connected = wentOffHook)
                }
                wasInCall = false
                wentOffHook = false
                lastState = TelephonyManager.EXTRA_STATE_IDLE
            }
        }
    }

    private fun onCallEnded(context: Context, connected: Boolean) {
        val status = DeviceStatus.fromApi(prefs.deviceStatus)
        if (status != DeviceStatus.APPROVED || prefs.activatedAtEpochMs <= 0L) return

        // Always capture + sync the call log (including missed/rejected calls)
        // and upload any recording.
        scheduler.requestImmediateSync()
        scheduler.requestRecordingUpload()

        // Only show the after-call popup for CONNECTED calls. Missed/rejected
        // calls are still logged but never trigger a popup.
        if (connected && prefs.callPopupEnabled && permissionManager.canDrawOverlays()) {
            CallPopupActivity.launch(context, lastNumber)
        }
        lastNumber = null
    }

    companion object {
        @Volatile private var lastState: String? = null
        @Volatile private var wasInCall: Boolean = false
        @Volatile private var wentOffHook: Boolean = false
        @Volatile private var lastNumber: String? = null
    }
}
