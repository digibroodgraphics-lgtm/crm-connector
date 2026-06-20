package com.digibrood.crmconnector.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.repository.CallRepository
import com.digibrood.crmconnector.data.repository.CapturedCallRef
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.overlay.CallPopupActivity
import com.digibrood.crmconnector.util.PermissionManager
import com.digibrood.crmconnector.util.PhoneUtils
import com.digibrood.crmconnector.worker.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Detects call start/end transitions. When a call ends and the device is
 * approved, it captures the just-ended call (generating ONE stable
 * client_call_id shared by sync, recording upload and the remark popup),
 * triggers sync, and — for connected calls only — shows the after-call popup.
 *
 * Missed/rejected calls are still captured and logged, but never show a popup.
 */
@AndroidEntryPoint
class CallReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: SyncScheduler
    @Inject lateinit var prefs: SecurePrefs
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var callRepository: CallRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
                if (!wasInCall) callStartAt = System.currentTimeMillis()
                wasInCall = true
                lastState = state
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (!wasInCall) callStartAt = System.currentTimeMillis()
                wasInCall = true
                wentOffHook = true
                lastState = state
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (wasInCall && lastState != TelephonyManager.EXTRA_STATE_IDLE) {
                    onCallEnded(context, connected = wentOffHook, startedAt = callStartAt)
                }
                wasInCall = false
                wentOffHook = false
                lastState = TelephonyManager.EXTRA_STATE_IDLE
            }
        }
    }

    private fun onCallEnded(context: Context, connected: Boolean, startedAt: Long) {
        val status = DeviceStatus.fromApi(prefs.deviceStatus)
        if (status != DeviceStatus.APPROVED || prefs.activatedAtEpochMs <= 0L) return

        val number = lastNumber
        val pending = goAsync()
        scope.launch {
            try {
                // The call log entry may take a moment to appear; retry briefly to
                // capture the just-ended call and obtain its stable client_call_id.
                var ref: CapturedCallRef? = null
                var attempts = 0
                while (ref == null && attempts < 6) {
                    ref = runCatching { callRepository.captureRecentCall(startedAt) }.getOrNull()
                    if (ref == null) {
                        delay(800)
                        attempts++
                    }
                }

                // Push the queue (uploads the call and any recording).
                scheduler.requestImmediateSync()
                scheduler.requestRecordingUpload()

                // Connected calls show the popup with the SAME id + call_type so the
                // remark links to the call and recording. Missed/rejected: no popup.
                if (connected && prefs.callPopupEnabled && permissionManager.canDrawOverlays()) {
                    if (ref != null) {
                        CallPopupActivity.launch(context, ref.phone, ref.clientCallId, ref.callType)
                    } else {
                        CallPopupActivity.launch(context, number, null, null)
                    }
                }
            } finally {
                lastNumber = null
                pending.finish()
            }
        }
    }

    companion object {
        @Volatile private var lastState: String? = null
        @Volatile private var wasInCall: Boolean = false
        @Volatile private var wentOffHook: Boolean = false
        @Volatile private var lastNumber: String? = null
        @Volatile private var callStartAt: Long = 0L
    }
}
