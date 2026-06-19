package com.digibrood.crmconnector.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure on-device storage backed by [EncryptedSharedPreferences].
 *
 * Holds the JWT access/refresh tokens, the configured CRM origin and a small
 * amount of session metadata. All values are encrypted at rest using a key from
 * the Android Keystore.
 */
@Singleton
class SecurePrefs @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _loggedIn = MutableStateFlow(accessToken != null)
    val loggedIn: StateFlow<Boolean> = _loggedIn.asStateFlow()

    // ---- JWT tokens ----
    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()
            _loggedIn.value = value != null
        }

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var tokenExpiryEpochMs: Long
        get() = prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        set(value) = prefs.edit().putLong(KEY_TOKEN_EXPIRY, value).apply()

    // ---- CRM origin (scheme://host[:port]) ----
    var crmOrigin: String?
        get() = prefs.getString(KEY_CRM_ORIGIN, null)
        set(value) = prefs.edit().putString(KEY_CRM_ORIGIN, value).apply()

    // ---- Session metadata ----
    var registeredNumber: String?
        get() = prefs.getString(KEY_REGISTERED_NUMBER, null)
        set(value) = prefs.edit().putString(KEY_REGISTERED_NUMBER, value).apply()

    var deviceStatus: String?
        get() = prefs.getString(KEY_DEVICE_STATUS, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_STATUS, value).apply()

    /** Epoch millis after which calls are eligible to be synced (forward-only). */
    var activatedAtEpochMs: Long
        get() = prefs.getLong(KEY_ACTIVATED_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_ACTIVATED_AT, value).apply()

    var callPopupEnabled: Boolean
        get() = prefs.getBoolean(KEY_CALL_POPUP_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_CALL_POPUP_ENABLED, value).apply()

    var recordingPathOverride: String?
        get() = prefs.getString(KEY_RECORDING_PATH_OVERRIDE, null)
        set(value) = prefs.edit().putString(KEY_RECORDING_PATH_OVERRIDE, value).apply()

    var brandingLogoUrl: String?
        get() = prefs.getString(KEY_BRANDING_LOGO, null)
        set(value) = prefs.edit().putString(KEY_BRANDING_LOGO, value).apply()

    var manualLogoUriOverride: String?
        get() = prefs.getString(KEY_MANUAL_LOGO, null)
        set(value) = prefs.edit().putString(KEY_MANUAL_LOGO, value).apply()

    var lastSyncEpochMs: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()

    var lastSyncResult: String?
        get() = prefs.getString(KEY_LAST_SYNC_RESULT, null)
        set(value) = prefs.edit().putString(KEY_LAST_SYNC_RESULT, value).apply()

    val isLoggedIn: Boolean get() = accessToken != null

    val isDeviceRegistered: Boolean get() = !registeredNumber.isNullOrBlank()

    /** Stores both tokens and the expiry timestamp atomically. */
    fun saveTokens(access: String?, refresh: String?, expiresInSeconds: Long?) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, access)
            if (refresh != null) putString(KEY_REFRESH_TOKEN, refresh)
            if (expiresInSeconds != null && expiresInSeconds > 0) {
                putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + expiresInSeconds * 1000L)
            }
        }.apply()
        _loggedIn.value = access != null
    }

    /** Wipes all session data. Used only by the CRM-driven end-of-session flow. */
    fun clearSession() {
        prefs.edit().clear().apply()
        _loggedIn.value = false
    }

    /**
     * Clears only the JWT tokens so the app routes back to login for a fresh
     * sign-in, while keeping the CRM origin, registered number and device status.
     * Used when the refresh token is rejected (e.g. server redeploy / secret
     * rotation) and the session can no longer be recovered automatically.
     */
    fun clearTokensForReauth() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()
        _loggedIn.value = false
    }

    companion object {
        private const val FILE_NAME = "crm_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_CRM_ORIGIN = "crm_origin"
        private const val KEY_REGISTERED_NUMBER = "registered_number"
        private const val KEY_DEVICE_STATUS = "device_status"
        private const val KEY_ACTIVATED_AT = "activated_at"
        private const val KEY_CALL_POPUP_ENABLED = "call_popup_enabled"
        private const val KEY_RECORDING_PATH_OVERRIDE = "recording_path_override"
        private const val KEY_BRANDING_LOGO = "branding_logo_url"
        private const val KEY_MANUAL_LOGO = "manual_logo_uri"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_LAST_SYNC_RESULT = "last_sync_result"
    }
}
