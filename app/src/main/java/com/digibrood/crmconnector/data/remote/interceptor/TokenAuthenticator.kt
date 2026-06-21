package com.digibrood.crmconnector.data.remote.interceptor

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.ApiError
import com.digibrood.crmconnector.data.remote.dto.RefreshRequest
import com.digibrood.crmconnector.di.RefreshClient
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * Refreshes an expired JWT when the CRM returns HTTP 401, and retries the
 * request once with the new token.
 *
 * NEVER silently logs out on transient errors (network, 5xx, a single expired
 * access token). The session/refresh token is only cleared when the CRM
 * explicitly returns code APP_LOGIN_DISABLED — i.e. the admin disabled mobile
 * login / revoked the device. Everything else keeps the session so WorkManager
 * can retry later with backoff.
 *
 * Uses a dedicated [CrmApiService] ([RefreshClient]) without this authenticator,
 * preventing infinite refresh recursion.
 */
class TokenAuthenticator @Inject constructor(
    private val prefs: SecurePrefs,
    @RefreshClient private val refreshApi: Provider<CrmApiService>,
    private val moshi: Moshi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Explicit "mobile login disabled / device revoked" -> end the session.
        if (parseErrorCode(response).equals("APP_LOGIN_DISABLED", ignoreCase = true)) {
            prefs.clearTokensForReauth()
            return null
        }

        // Stop retrying after two attempts to avoid loops.
        if (responseCount(response) >= 2) return null

        val refreshToken = prefs.refreshToken ?: return null
        val attemptedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")?.trim()

        val newAccessToken: String? = synchronized(this) {
            val current = prefs.accessToken
            // Another thread may have already refreshed the token.
            if (!current.isNullOrBlank() && current != attemptedToken) {
                current
            } else {
                refreshBlocking(refreshToken)
            }
        }

        if (newAccessToken.isNullOrBlank()) return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun refreshBlocking(refreshToken: String): String? = runBlocking {
        try {
            val result = refreshApi.get().refresh(RefreshRequest(refreshToken))
            if (result.isSuccessful) {
                val body = result.body()
                prefs.saveTokens(body?.accessToken, body?.refreshToken, body?.expiresIn)
                body?.accessToken
            } else {
                // Do NOT clear the session here — keep the refresh token and let the
                // app retry later (the CRM may be redeploying / temporarily down).
                null
            }
        } catch (t: Throwable) {
            null
        }
    }

    private fun parseErrorCode(response: Response): String? {
        return try {
            val raw = response.peekBody(2048).string()
            if (raw.isBlank()) null else moshi.adapter(ApiError::class.java).fromJson(raw)?.errorCode
        } catch (t: Throwable) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
