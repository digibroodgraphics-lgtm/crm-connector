package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.MetaStatus
import com.digibrood.crmconnector.data.remote.dto.MetaTag
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caches the CRM's tags/statuses/stages (GET /meta) so the after-call popup can
 * show up-to-date dropdowns. Refreshed on each sync cycle.
 */
@Singleton
class MetaRepository @Inject constructor(
    private val api: CrmApiService,
    private val moshi: Moshi
) {
    @Volatile
    var statuses: List<MetaStatus> = DEFAULT_STATUSES
        private set

    @Volatile
    var tags: List<MetaTag> = emptyList()
        private set

    suspend fun refresh() {
        when (val result = safeApiCall(moshi) { api.getMeta() }) {
            is NetworkResult.Success -> {
                val body = result.data
                if (body.statuses.isNotEmpty()) statuses = body.statuses
                tags = body.tags
            }
            else -> Unit // keep cached values on failure
        }
    }

    companion object {
        val DEFAULT_STATUSES = listOf(
            MetaStatus("lead", "Lead"),
            MetaStatus("enquiry", "Enquiry"),
            MetaStatus("paid", "Paid client"),
            MetaStatus("lost", "Lost")
        )
    }
}
