package com.digibrood.crmconnector.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.digibrood.crmconnector.data.repository.CallRepository
import com.digibrood.crmconnector.util.Constants
import com.digibrood.crmconnector.worker.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Best-effort capture of VoIP / app calls (WhatsApp, Telegram, Signal, Messenger,
 * etc.) by observing their CALL notifications (D6). These calls never appear in
 * the system call log, so this is the only practical way to log them.
 *
 * How it works:
 *  - When a target app posts a call-style notification (category CALL or a
 *    CallStyle template), we record the call start and the displayed name.
 *  - When that notification is removed (call ended/declined), we enqueue a VoIP
 *    call with the measured duration, tagged with the app package as `platform`,
 *    and trigger an immediate sync. The CRM matches/creates the contact by name.
 *
 * Limitations (documented, best-effort): direction can't be reliably derived from
 * a notification, so connected VoIP calls are logged as "incoming"; audio of VoIP
 * calls cannot be recorded on modern Android. Requires the user to grant
 * Notification Access. Never blocks the core PSTN flow.
 */
@AndroidEntryPoint
class VoipCallListenerService : NotificationListenerService() {

    @Inject lateinit var callRepository: CallRepository
    @Inject lateinit var scheduler: SyncScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val active = ConcurrentHashMap<String, ActiveCall>()

    private data class ActiveCall(val platform: String, val name: String?, val startTime: Long)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val pkg = sbn.packageName ?: return
        if (pkg !in Constants.VOIP_PACKAGES) return
        if (!isCallNotification(notification)) return

        val key = sbn.key ?: "$pkg:${sbn.id}"
        if (active.containsKey(key)) return // already tracking this call
        active[key] = ActiveCall(
            platform = pkg,
            name = extractName(notification),
            startTime = System.currentTimeMillis()
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        val key = sbn?.key ?: return
        val call = active.remove(key) ?: return
        val endTime = System.currentTimeMillis()
        scope.launch {
            runCatching {
                val id = callRepository.enqueueVoipCall(
                    platform = call.platform,
                    name = call.name,
                    callType = "incoming",
                    startTime = call.startTime,
                    endTime = endTime
                )
                if (id != null) scheduler.requestImmediateSync()
            }
        }
    }

    private fun isCallNotification(n: Notification): Boolean {
        if (n.category == Notification.CATEGORY_CALL) return true
        val template = n.extras?.getString(Notification.EXTRA_TEMPLATE)
        return template != null && template.endsWith("CallStyle")
    }

    private fun extractName(n: Notification): String? {
        val extras = n.extras ?: return null
        val convo = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        return listOf(convo, title, text)
            .firstOrNull { !it.isNullOrBlank() }
            ?.trim()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
