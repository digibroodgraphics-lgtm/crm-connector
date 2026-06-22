package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.LoginRequest
import com.digibrood.crmconnector.data.remote.dto.RefreshRequest
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.di.RefreshClient
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.util.TimeUtils
import com.digibrood.crmconnector.util.UrlValidator
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Outcome of a login attempt, expressed in terms the UI can act on directly. */
sealed interface LoginResult {
    data class Success(val status: DeviceStatus) : LoginResult
    data object LoginDisabled : LoginResult
    data object InvalidCredentials : LoginResult
    data object InvalidUrl : LoginResult
    data object NotHttps : LoginResult
    data object EmptyUrl : LoginResult
    data object NetworkError : LoginResult
    data class Error(val message: String?) : LoginResult
}

/**
 * Handles authentication against the CRM: validates the CRM URL (HTTPS only),
 * stores the resulting JWT securely and records the initial device status.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: CrmApiService,
    @RefreshClient private val refreshApi: Provider<CrmApiService>,
    private val moshi: Moshi,
    private val prefs: SecurePrefs
) {

    suspend fun login(crmUrl: String, email: String, password: String): LoginResult {
        when (val v = UrlValidator.validate(crmUrl)) {
            is UrlValidator.Result.Valid -> prefs.crmOrigin = v.origin
            UrlValidator.Result.Empty -> return LoginResult.EmptyUrl
            UrlValidator.Result.NotHttps -> return LoginResult.NotHttps
            UrlValidator.Result.Malformed -> return LoginResult.InvalidUrl
        }

        return when (val result = safeApiCall(moshi) {
            api.login(LoginRequest(email = email.trim(), password = password))
        }) {
            is NetworkResult.Success -> {
                val body = result.data
                if (body.accessToken.isNullOrBlank()) {
                    // Signed in but no usable token field was found in the response.
                    LoginResult.Error(
                        "Signed in, but no login token was returned. Your CRM developer " +
                            "should confirm the login response contains \"access_token\"."
                    )
                } else {
                    prefs.saveTokens(body.accessToken, body.refreshToken, body.expiresIn)
                    // Persist credentials (encrypted) so the app can silently
                    // re-login if the refresh token is ever rejected.
                    prefs.savedEmail = email.trim()
                    prefs.savedPassword = password
                    body.deviceStatus?.let { prefs.deviceStatus = it }
                    body.registeredNumber?.let { prefs.registeredNumber = it }
                    if (!body.activatedAt.isNullOrBlank()) {
                        prefs.activatedAtEpochMs = TimeUtils.parseToEpochMillis(body.activatedAt)
                    }
                    LoginResult.Success(DeviceStatus.fromApi(body.deviceStatus))
                }
            }

            is NetworkResult.ApiFailure -> {
                when {
                    result.errorCode.equals("APP_LOGIN_DISABLED", true) -> LoginResult.LoginDisabled
                    result.errorCode.equals("INVALID_CREDENTIALS", true) -> LoginResult.InvalidCredentials
                    result.httpCode == 401 || result.httpCode == 403 -> LoginResult.InvalidCredentials
                    else -> LoginResult.Error(result.message)
                }
            }

            is NetworkResult.NetworkError -> LoginResult.NetworkError
        }
    }

    val isLoggedIn: Boolean get() = prefs.isLoggedIn

    /**
     * Proactively refreshes the access token shortly BEFORE it expires so there
     * is never a visible interruption. Called periodically by the foreground
     * service. No-op if there is no expiry recorded or it is not near expiry.
     */
    suspend fun proactiveRefreshIfNeeded() {
        val refresh = prefs.refreshToken ?: return
        val expiry = prefs.tokenExpiryEpochMs
        if (expiry <= 0L) return
        // Refresh when within 1 hour of the access token expiring.
        if (System.currentTimeMillis() < expiry - 60 * 60 * 1000L) return
        runCatching {
            val res = refreshApi.get().refresh(RefreshRequest(refresh))
            if (res.isSuccessful) {
                val b = res.body()
                if (!b?.accessToken.isNullOrBlank()) {
                    prefs.saveTokens(b?.accessToken, b?.refreshToken, b?.expiresIn)
                }
            }
        }
    }
}
