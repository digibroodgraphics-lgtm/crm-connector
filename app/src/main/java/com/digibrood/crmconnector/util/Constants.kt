package com.digibrood.crmconnector.util

import android.Manifest
import android.os.Build

/**
 * App-wide constant values. Centralised so that worker tags, notification ids,
 * preference keys and the permission list are defined in exactly one place.
 */
object Constants {

    // ---- Networking ----
    const val API_PATH = "api/mobile/v1/"
    const val MAX_RECORDING_BYTES = 50L * 1024L * 1024L // 50 MB hard limit

    // ---- Notifications ----
    const val SYNC_CHANNEL_ID = "crm_sync_channel"
    const val SYNC_NOTIFICATION_ID = 1001
    const val UPLOAD_NOTIFICATION_ID = 1002

    // ---- WorkManager unique work names / tags ----
    const val WORK_PERIODIC_SYNC = "crm_periodic_sync"
    const val WORK_ONE_TIME_SYNC = "crm_one_time_sync"
    const val WORK_RECORDING_UPLOAD = "crm_recording_upload"
    const val WORK_HEARTBEAT = "crm_heartbeat"
    const val WORK_TAG = "crm_connector_work"

    // ---- Call popup intent extras ----
    const val EXTRA_POPUP_PHONE = "extra_popup_phone"
    const val EXTRA_POPUP_CLIENT_CALL_ID = "extra_popup_client_call_id"
    const val EXTRA_POPUP_CALL_TYPE = "extra_popup_call_type"

    // ---- Sync timing ----
    const val DEFAULT_SYNC_INTERVAL_MINUTES = 15L
    const val DEFAULT_HEARTBEAT_INTERVAL_MINUTES = 30L

    // ---- Default recording scan paths ----
    val DEFAULT_RECORDING_PATHS = listOf(
        "/storage/emulated/0/Recordings/Call/",
        "/storage/emulated/0/Recordings/",
        "/storage/emulated/0/Sounds/Call/"
    )

    val SUPPORTED_RECORDING_EXTENSIONS = listOf("mp3", "m4a", "wav", "amr", "3gp")

    /**
     * Runtime permissions that must be granted before any sync work begins.
     * The list adapts to the running Android version because the storage and
     * notification permission model changed across releases.
     */
    val requiredRuntimePermissions: List<String>
        get() {
            val perms = mutableListOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS,
                Manifest.permission.READ_CONTACTS
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.READ_MEDIA_AUDIO)
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                @Suppress("DEPRECATION")
                perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            return perms
        }
}
