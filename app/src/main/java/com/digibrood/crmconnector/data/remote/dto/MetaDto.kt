package com.digibrood.crmconnector.data.remote.dto

import com.squareup.moshi.Json

/** A CRM tag (multi-select in the popup). */
data class MetaTag(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

/** A CRM contact status option (single-select in the popup). */
data class MetaStatus(
    @Json(name = "value") val value: String,
    @Json(name = "label") val label: String
)

/** A CRM pipeline stage. */
data class MetaStage(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

/** GET /meta response: dropdown options for the after-call popup. */
data class MetaResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "tags") val tags: List<MetaTag> = emptyList(),
    @Json(name = "statuses") val statuses: List<MetaStatus> = emptyList(),
    @Json(name = "stages") val stages: List<MetaStage> = emptyList()
)
