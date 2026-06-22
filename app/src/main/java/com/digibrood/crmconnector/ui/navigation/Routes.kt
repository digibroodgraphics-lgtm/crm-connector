package com.digibrood.crmconnector.ui.navigation

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.util.PermissionManager
import javax.inject.Inject
import javax.inject.Singleton

/** Navigation route constants. */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val PERMISSIONS = "permissions"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val WHITELIST = "whitelist"
}

/**
 * Single source of truth for "where should the user go next". Used by the splash
 * screen for auto-resume and by login/permissions/register after each step.
 */
@Singleton
class StartDestinationProvider @Inject constructor(
    private val prefs: SecurePrefs,
    private val permissionManager: PermissionManager
) {
    fun decide(): String = when {
        !prefs.isLoggedIn -> Routes.LOGIN
        !permissionManager.allEssentialGranted() -> Routes.PERMISSIONS
        !prefs.isDeviceRegistered -> Routes.REGISTER
        else -> Routes.DASHBOARD
    }
}
