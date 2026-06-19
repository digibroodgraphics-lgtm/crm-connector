package com.digibrood.crmconnector.data.remote.interceptor

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches the JWT access token as a Bearer Authorization header to every
 * authenticated request. Login and refresh endpoints are skipped because they
 * either issue or renew the token.
 */
class AuthInterceptor @Inject constructor(
    private val prefs: SecurePrefs
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        val builder = original.newBuilder()
            .header("Accept", "application/json")

        val isAuthEndpoint = path.endsWith("auth/login") || path.endsWith("auth/refresh")
        val token = prefs.accessToken
        if (!isAuthEndpoint && !token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
