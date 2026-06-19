package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.LoginRequest
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.domain.model.DeviceStatus
import com.digibrood.crmconnector.util.TimeUtils
import com.digibrood.crmconnector.util.UrlValidator
import com.squareup.moshi.Moshi
import javax.inject.Inject
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
                prefs.saveTokens(body.accessToken, body.refreshToken, body.expiresIn)
                body.deviceStatus?.let { prefs.deviceStatus = it }
                body.registeredNumber?.let { prefs.registeredNumber = it }
                if (!body.activatedAt.isNullOrBlank()) {
                    prefs.activatedAtEpochMs = TimeUtils.parseToEpochMillis(body.activatedAt)
                }
                LoginResult.Success(DeviceStatus.fromApi(body.deviceStatus))
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
}
