package com.digibrood.crmconnector.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.digibrood.crmconnector.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supplies stable identifiers and descriptive metadata about the current device.
 * Used for device registration and the heartbeat payload.
 */
@Singleton
class DeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @SuppressLint("HardwareIds")
    val deviceId: String by lazy {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        androidId?.takeIf { it.isNotBlank() && it != "9774d56d682e549c" }
            ?: "${Build.MANUFACTURER}-${Build.MODEL}-${Build.SERIAL}".filter { it.isLetterOrDigit() }
    }

    val deviceName: String
        get() {
            val configured = Settings.Global.getString(context.contentResolver, "device_name")
            return configured?.takeIf { it.isNotBlank() } ?: "${Build.MANUFACTURER} ${Build.MODEL}"
        }

    val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

    val osVersion: String
        get() = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"

    val appVersion: String
        get() = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
}
