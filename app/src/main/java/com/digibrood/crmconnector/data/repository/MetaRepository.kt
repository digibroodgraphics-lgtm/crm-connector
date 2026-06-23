package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.MetaStatus
import com.digibrood.crmconnector.data.remote.dto.MetaTag
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caches the CRM's tags/statuses/stages (GET /meta) so the after-call popup can
 * show up-to-date dropdowns. Refreshed on each sync cycle AND persisted to disk
 * so the Status/Tags dropdowns always appear in the popup — even on first launch,
 * offline, or while a token error is being recovered.
 */
@Singleton
class MetaRepository @Inject constructor(
    private val api: CrmApiService,
    private val moshi: Moshi,
    private val prefs: SecurePrefs
) {
    @Volatile
    var statuses: List<MetaStatus> = DEFAULT_STATUSES
        private set

    @Volatile
    var tags: List<MetaTag> = emptyList()
        private set

    private val tagsAdapter by lazy {
        moshi.adapter<List<MetaTag>>(Types.newParameterizedType(List::class.java, MetaTag::class.java))
    }
    private val statusesAdapter by lazy {
        moshi.adapter<List<MetaStatus>>(Types.newParameterizedType(List::class.java, MetaStatus::class.java))
    }

    init {
        // Load the last-known meta so dropdowns are populated immediately.
        runCatching {
            prefs.cachedTagsJson?.let { json -> tagsAdapter.fromJson(json)?.let { tags = it } }
            prefs.cachedStatusesJson?.let { json ->
                statusesAdapter.fromJson(json)?.let { if (it.isNotEmpty()) statuses = it }
            }
        }
    }

    suspend fun refresh() {
        when (val result = safeApiCall(moshi) { api.getMeta() }) {
            is NetworkResult.Success -> {
                val body = result.data
                if (body.statuses.isNotEmpty()) statuses = body.statuses
                tags = body.tags
                // Persist so the popup always has the latest dropdowns.
                runCatching {
                    prefs.cachedTagsJson = tagsAdapter.toJson(tags)
                    prefs.cachedStatusesJson = statusesAdapter.toJson(statuses)
                }
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
