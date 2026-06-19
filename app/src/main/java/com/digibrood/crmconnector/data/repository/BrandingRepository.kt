package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.BrandingResponse
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.util.UrlValidator
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches branding (logo) from the CRM. Resolution order for the splash/login
 * logo is: manual override -> remote branding logo -> bundled fallback drawable.
 */
@Singleton
class BrandingRepository @Inject constructor(
    private val api: CrmApiService,
    private val moshi: Moshi,
    private val prefs: SecurePrefs
) {

    suspend fun refreshBranding(): NetworkResult<BrandingResponse> {
        val result = safeApiCall(moshi) { api.getBranding() }
        if (result is NetworkResult.Success) {
            val logo = result.data.logoUrl
            // Only accept secure https logo URLs.
            if (UrlValidator.isHttps(logo)) {
                prefs.brandingLogoUrl = logo
            }
        }
        return result
    }

    /**
     * Returns a Coil-compatible model for the current logo:
     * a String URL when a remote/manual logo is available, otherwise the local
     * fallback drawable resource id (Int).
     */
    fun logoModel(): Any {
        prefs.manualLogoUriOverride?.takeIf { it.isNotBlank() }?.let { return it }
        prefs.brandingLogoUrl?.takeIf { UrlValidator.isHttps(it) }?.let { return it }
        return R.drawable.ic_logo_fallback
    }

    fun setManualLogoOverride(uri: String?) {
        prefs.manualLogoUriOverride = uri?.takeIf { it.isNotBlank() }
    }
}
