package com.digibrood.crmconnector.data.remote.interceptor

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.ApiError
import com.digibrood.crmconnector.data.remote.dto.LoginRequest
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
 * Keeps the session alive transparently when the CRM returns HTTP 401.
 *
 * Recovery ladder (so syncing NEVER visibly stops on a token error):
 *  1. On 401 (INVALID_TOKEN / TOKEN_EXPIRED / NO_TOKEN), refresh the access
 *     token with the saved refresh_token and retry the request.
 *  2. If the refresh itself fails (e.g. the server rotated secrets on redeploy),
 *     silently RE-LOGIN with the stored credentials, then retry.
 *  3. Only an explicit APP_LOGIN_DISABLED ends the session here. (DEVICE_REVOKED
 *     / APP_LOGIN_DISABLED also arrive as 403 and are handled by the
 *     SessionGuardInterceptor.) Everything else keeps the session so WorkManager
 *     can retry with backoff.
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
        val error = parseError(response)
        val code = error?.errorCode
        val action = error?.action

        // Explicit "mobile login disabled" -> end the session.
        if (code.equals("APP_LOGIN_DISABLED", ignoreCase = true)) {
            prefs.clearTokensForReauth()
            return null
        }

        // Stop retrying after two attempts to avoid loops.
        if (responseCount(response) >= 3) return null

        val attemptedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")?.trim()

        // The CRM signals what to do: action="reauth" (INVALID_TOKEN/NO_TOKEN) means
        // the stored token can't be refreshed and we must log in again; action="refresh"
        // (TOKEN_EXPIRED) means refresh first. Fall back across both regardless.
        val reauthOnly = action.equals("reauth", ignoreCase = true) ||
            code.equals("INVALID_TOKEN", ignoreCase = true) ||
            code.equals("NO_TOKEN", ignoreCase = true)

        val newAccessToken: String? = synchronized(this) {
            val current = prefs.accessToken
            // Another thread may have already refreshed the token.
            if (!current.isNullOrBlank() && current != attemptedToken) {
                current
            } else {
                // 1) For refreshable errors, try refresh with the saved refresh token.
                val refreshed = if (!reauthOnly) {
                    val refreshToken = prefs.refreshToken
                    if (!refreshToken.isNullOrBlank()) refreshBlocking(refreshToken) else null
                } else {
                    null
                }
                // 2) Fall back to a silent re-login with stored credentials.
                refreshed ?: reloginBlocking()
            }
        }

        if (newAccessToken.isNullOrBlank()) {
            // Couldn't recover. If there are no stored credentials to re-login with
            // (e.g. upgraded from an old build), route to the login screen so the
            // user can sign in once; otherwise keep the session and let it retry.
            if (!prefs.hasSavedCredentials) prefs.clearTokensForReauth()
            return null
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun refreshBlocking(refreshToken: String): String? = runBlocking {
        try {
            val result = refreshApi.get().refresh(RefreshRequest(refreshToken))
            if (result.isSuccessful) {
                val body = result.body()
                if (body?.accessToken.isNullOrBlank()) return@runBlocking null
                prefs.saveTokens(body?.accessToken, body?.refreshToken, body?.expiresIn)
                body?.accessToken
            } else {
                // Do NOT clear the session here — the re-login fallback handles it.
                null
            }
        } catch (t: Throwable) {
            null
        }
    }

    /**
     * Silent re-login using the encrypted stored credentials. Recovers the
     * session when the refresh token is rejected (e.g. server redeploy / secret
     * rotation) without ever showing the login screen.
     */
    private fun reloginBlocking(): String? = runBlocking {
        val email = prefs.savedEmail
        val password = prefs.savedPassword
        if (email.isNullOrBlank() || password.isNullOrBlank()) return@runBlocking null
        try {
            val result = refreshApi.get().login(LoginRequest(email = email, password = password))
            if (result.isSuccessful) {
                val body = result.body()
                if (body?.accessToken.isNullOrBlank()) return@runBlocking null
                prefs.saveTokens(body?.accessToken, body?.refreshToken, body?.expiresIn)
                body?.accessToken
            } else {
                null
            }
        } catch (t: Throwable) {
            null
        }
    }

    private fun parseError(response: Response): ApiError? {
        return try {
            val raw = response.peekBody(2048).string()
            if (raw.isBlank()) null else moshi.adapter(ApiError::class.java).fromJson(raw)
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
