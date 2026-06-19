package com.digibrood.crmconnector.data.remote.interceptor

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Rewrites the scheme/host/port of every outgoing request to the CRM origin the
 * user configured at login, while preserving the fixed "/api/mobile/v1/" path.
 *
 * This lets the same Retrofit instance talk to any customer's CRM without
 * rebuilding it. The API path is identical for every CRM per the contract.
 */
class DynamicBaseUrlInterceptor @Inject constructor(
    private val prefs: SecurePrefs
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val origin = prefs.crmOrigin?.toHttpUrlOrNull()

        val request = if (origin != null) {
            val newUrl = original.url.newBuilder()
                .scheme(origin.scheme)
                .host(origin.host)
                .port(origin.port)
                .build()
            original.newBuilder().url(newUrl).build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
