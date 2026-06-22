package com.digibrood.crmconnector.data.remote.interceptor

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.dto.ApiError
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Detects the ONLY two conditions that should end a device session and routes
 * the app back to login:
 *  - HTTP 403 with code APP_LOGIN_DISABLED  (admin disabled mobile login)
 *  - HTTP 403 with code DEVICE_REVOKED      (admin revoked this device)
 *
 * The OkHttp [okhttp3.Authenticator] only fires on HTTP 401, so these 403 codes
 * would otherwise never trigger a logout. Everything else (network errors, 5xx,
 * a single expired access token / 401 TOKEN_EXPIRED, other 403s) is left to the
 * [TokenAuthenticator]'s refresh-and-retry path and NEVER drops the session —
 * satisfying the "app must never silently log out" requirement (D2).
 */
class SessionGuardInterceptor @Inject constructor(
    private val prefs: SecurePrefs,
    private val moshi: Moshi
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 403) {
            val code = parseErrorCode(response)
            if (code.equals("APP_LOGIN_DISABLED", ignoreCase = true) ||
                code.equals("DEVICE_REVOKED", ignoreCase = true)
            ) {
                prefs.clearTokensForReauth()
            }
        }
        return response
    }

    /** Reads the error code without consuming the response body. */
    private fun parseErrorCode(response: Response): String? {
        return try {
            val raw = response.peekBody(2048).string()
            if (raw.isBlank()) null
            else moshi.adapter(ApiError::class.java).fromJson(raw)?.errorCode
        } catch (t: Throwable) {
            null
        }
    }
}
