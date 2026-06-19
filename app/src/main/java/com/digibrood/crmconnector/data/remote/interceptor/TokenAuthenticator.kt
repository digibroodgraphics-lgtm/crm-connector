package com.digibrood.crmconnector.data.remote.interceptor

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.RefreshRequest
import com.digibrood.crmconnector.di.RefreshClient
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

/**
 * Transparently refreshes an expired JWT when the CRM returns HTTP 401.
 *
 * Uses a dedicated [CrmApiService] ([RefreshClient]) that does NOT itself carry
 * this authenticator, preventing infinite recursion. Access to the refresh call
 * is synchronised so concurrent 401s only trigger a single refresh.
 */
class TokenAuthenticator @Inject constructor(
    private val prefs: SecurePrefs,
    @RefreshClient private val refreshApi: Provider<CrmApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
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
                // Refresh token is no longer valid (e.g. server redeployed / secret
                // rotated). Clear the dead session so the app routes back to login.
                prefs.clearTokensForReauth()
                null
            }
        } catch (t: Throwable) {
            // Transient/network error — keep the session and retry later.
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
