package com.digibrood.crmconnector.util

import android.util.Patterns

/**
 * Validates and normalises the CRM URL entered by the user.
 *
 * Security requirement: only https:// origins are accepted. Any http:// URL is
 * rejected outright so call data is never transmitted over cleartext.
 */
object UrlValidator {

    sealed interface Result {
        data class Valid(val origin: String) : Result
        data object Empty : Result
        data object NotHttps : Result
        data object Malformed : Result
    }

    /**
     * Validates [input] and, on success, returns the canonical origin
     * (scheme://host[:port]) with no trailing slash.
     */
    fun validate(input: String?): Result {
        val raw = input?.trim().orEmpty()
        if (raw.isEmpty()) return Result.Empty

        // If the user typed no scheme, do NOT silently assume http; require https explicitly.
        val candidate = if (raw.contains("://")) raw else "https://$raw"

        if (!candidate.startsWith("https://", ignoreCase = true)) {
            return Result.NotHttps
        }

        val withoutScheme = candidate.removePrefix("https://").removePrefix("HTTPS://")
        val hostPort = withoutScheme.substringBefore('/').substringBefore('?')
        val host = hostPort.substringBefore(':')

        if (host.isBlank()) return Result.Malformed
        if (!Patterns.DOMAIN_NAME.matcher(host).matches()) return Result.Malformed

        val origin = "https://$hostPort".trimEnd('/')
        return Result.Valid(origin)
    }

    /** Returns true only for https origins; used as a last line of defence. */
    fun isHttps(url: String?): Boolean =
        url?.trim()?.startsWith("https://", ignoreCase = true) == true
}
