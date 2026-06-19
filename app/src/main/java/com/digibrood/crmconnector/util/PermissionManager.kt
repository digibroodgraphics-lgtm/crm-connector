package com.digibrood.crmconnector.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralised checks for the runtime and special permissions the app relies on.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /** True when every runtime permission required for sync is granted. */
    fun areCorePermissionsGranted(): Boolean =
        Constants.requiredRuntimePermissions.all { isGranted(it) }

    /** "Display over other apps" special permission, required for the call popup. */
    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    /** Notifications are only a runtime permission on Android 13+. */
    fun areNotificationsAllowed(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }

    /**
     * True only when everything needed to operate is in place: core runtime
     * permissions, notifications and overlay access.
     */
    fun allEssentialGranted(): Boolean =
        areCorePermissionsGranted() && canDrawOverlays()
}
