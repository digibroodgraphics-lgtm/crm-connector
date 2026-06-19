package com.digibrood.crmconnector.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * Hard guarantee that no request ever leaves the device over cleartext.
 * Even though the network security config also forbids HTTP, this interceptor
 * fails fast with a clear error if a non-HTTPS URL is ever attempted.
 */
class HttpsEnforcementInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.url.isHttps) {
            throw IOException("Blocked insecure request: only HTTPS is allowed (${request.url}).")
        }
        return chain.proceed(request)
    }
}
