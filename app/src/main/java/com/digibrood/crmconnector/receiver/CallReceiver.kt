package com.digibrood.crmconnector.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.repository.CallRepository
import com.digibrood.crmconnector.domain.model.CallType
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.overlay.CallPopupActivity
import com.digibrood.crmconnector.util.CapturedCall
import com.digibrood.crmconnector.util.PermissionManager
import com.digibrood.crmconnector.util.PhoneUtils
import com.digibrood.crmconnector.worker.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Detects call start/end transitions from the phone-state broadcast and captures
 * the just-ended call DIRECTLY from the broadcast data (number + timing), then
 * queues it for upload and — for connected calls — shows the after-call popup.
 *
 * Capturing from the broadcast (rather than reading the system call log) is
 * deliberate: some OEM dialers (e.g. Samsung) expose a stale/incomplete call log
 * to apps, which previously caused recent calls to never be captured. Building
 * the record from the broadcast makes capture reliable and independent of the
 * call-log provider. Every call type is captured:
 *   - incoming answered  -> incoming
 *   - incoming not answered / rejected -> missed (no popup)
 *   - outgoing (answered or not) -> outgoing
 * so missed calls and unanswered outgoing calls still reach the CRM. Dismissing
 * the popup never drops the call — it's already queued before the popup shows.
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
                    if (it.isNotBlank()) lastNumber = PhoneUtils.normalize(it)
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
                sawRinging = true
                wasInCall = true
                lastState = state
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                val now = System.currentTimeMillis()
                if (!wasInCall) callStartAt = now
                if (offHookAt == 0L) offHookAt = now
                wasInCall = true
                wentOffHook = true
                lastState = state
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (wasInCall && lastState != TelephonyManager.EXTRA_STATE_IDLE) {
                    onCallEnded(
                        context = context,
                        sawRinging = sawRinging,
                        connected = wentOffHook,
                        startedAt = callStartAt,
                        offHookAt = offHookAt,
                        endedAt = System.currentTimeMillis()
                    )
                }
                // Reset the state machine for the next call.
                wasInCall = false
                wentOffHook = false
                sawRinging = false
                offHookAt = 0L
                lastState = TelephonyManager.EXTRA_STATE_IDLE
            }
        }
    }

    private fun onCallEnded(
        context: Context,
        sawRinging: Boolean,
        connected: Boolean,
        startedAt: Long,
        offHookAt: Long,
        endedAt: Long
    ) {
        val status = DeviceStatus.fromApi(prefs.deviceStatus)
        if (status != DeviceStatus.APPROVED || prefs.activatedAtEpochMs <= 0L) {
            lastNumber = null
            return
        }

        val number = lastNumber ?: ""
        // Direction/outcome from the broadcast state machine.
        val callType = when {
            sawRinging && connected -> CallType.INCOMING
            sawRinging && !connected -> CallType.MISSED
            else -> CallType.OUTGOING
        }
        // Talk window for connected calls (exclude ring time); 0 for missed.
        val talkStart = if (connected && offHookAt > 0L) offHookAt else startedAt
        val duration = if (connected) ((endedAt - talkStart).coerceAtLeast(0L)) / 1000L else 0L
        val captured = CapturedCall(
            phoneNumber = PhoneUtils.normalize(number),
            startTime = if (connected) talkStart else startedAt,
            endTime = endedAt,
            durationSeconds = duration,
            callType = callType
        )

        val pending = goAsync()
        scope.launch {
            try {
                // Queue the call NOW from broadcast data (independent of the call log).
                val clientCallId = runCatching { callRepository.enqueueCaptured(captured) }.getOrNull()

                // Push the queue and recordings (recording files arrive a bit later).
                scheduler.requestImmediateSync()
                scheduler.requestRecordingUpload()
                scheduler.scheduleDelayedRecordingUpload()

                // Connected calls show the popup with the SAME id + call_type so a
                // remark links to the call/recording. Missed/rejected: no popup, but
                // the call is already queued above.
                if (connected && number.isNotBlank() &&
                    prefs.callPopupEnabled && permissionManager.canDrawOverlays()
                ) {
                    CallPopupActivity.launch(context, number, clientCallId, callType.apiValue)
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
        @Volatile private var sawRinging: Boolean = false
        @Volatile private var lastNumber: String? = null
        @Volatile private var callStartAt: Long = 0L
        @Volatile private var offHookAt: Long = 0L
    }
}
