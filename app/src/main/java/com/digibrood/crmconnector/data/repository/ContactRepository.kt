package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.local.dao.RemarkDao
import com.digibrood.crmconnector.data.local.entity.RemarkEntity
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.ContactLookupResponse
import com.digibrood.crmconnector.data.remote.dto.RemarkRequest
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.util.CallLogReader
import com.digibrood.crmconnector.util.ContactReader
import com.digibrood.crmconnector.util.DeviceInfoProvider
import com.digibrood.crmconnector.util.PhoneUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Resolved contact details for the after-call popup. */
data class ContactDetails(
    val found: Boolean,
    val name: String?,
    val company: String?,
    val status: String?
)

/**
 * Contact lookups for the after-call popup and remark submission. Remarks are
 * queued locally and retried if the device is offline when the user saves them.
 */
@Singleton
class ContactRepository @Inject constructor(
    private val api: CrmApiService,
    private val moshi: Moshi,
    private val remarkDao: RemarkDao,
    private val deviceInfo: DeviceInfoProvider,
    private val contactReader: ContactReader,
    private val callLogReader: CallLogReader
) {

    /**
     * Best-effort name for an unknown number from the DEVICE: first the user's
     * saved contacts, then the caller-ID name a app such as Truecaller cached in
     * the call log for the most recent call. Returns null if nothing is found.
     */
    private fun deviceName(normalized: String): String? =
        contactReader.displayNameFor(normalized)?.takeIf { it.isNotBlank() }
            ?: callLogReader.cachedNameFor(normalized)?.takeIf { it.isNotBlank() }

    /**
     * Looks up a phone number against the CRM, returning the contact's name and
     * company for pre-filling the popup. When the CRM has no name (unknown number),
     * falls back to the device's contact name and then the caller-ID (Truecaller)
     * cached name, so the popup is pre-filled and the user can just edit/save.
     */
    suspend fun lookup(phoneNumber: String): ContactDetails = withContext(Dispatchers.IO) {
        val normalized = PhoneUtils.normalize(phoneNumber)
        when (val result = safeApiCall(moshi) { api.lookupContact(normalized) }) {
            is NetworkResult.Success -> {
                val body: ContactLookupResponse = result.data
                val crmName = body.effectiveName
                val name = crmName ?: deviceName(normalized)
                ContactDetails(
                    found = body.found || !crmName.isNullOrBlank(),
                    name = name,
                    company = body.effectiveCompany,
                    status = body.effectiveStatus
                )
            }
            else -> ContactDetails(
                found = false,
                name = deviceName(normalized),
                company = null,
                status = null
            )
        }
    }

    /**
     * Sends a remark to the CRM (phone + optional name/company + note). If the
     * call fails it is queued for retry; the method still reports success so the
     * popup can close cleanly.
     */
    suspend fun saveRemark(
        clientCallId: String?,
        phoneNumber: String,
        name: String?,
        company: String?,
        remark: String,
        status: String?,
        callType: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val normalized = PhoneUtils.normalize(phoneNumber)
        val request = RemarkRequest(
            deviceId = deviceInfo.deviceId,
            clientCallId = clientCallId,
            phone = normalized,
            callType = callType?.takeIf { it.isNotBlank() },
            name = name?.takeIf { it.isNotBlank() },
            company = company?.takeIf { it.isNotBlank() },
            remark = remark,
            status = status?.takeIf { it.isNotBlank() }
        )
        val result = safeApiCall(moshi) { api.saveRemark(request) }
        if (result is NetworkResult.Success) {
            true
        } else {
            remarkDao.insert(
                RemarkEntity(
                    clientCallId = clientCallId,
                    phoneNumber = normalized,
                    contactName = name,
                    company = company,
                    callType = callType,
                    remark = remark,
                    status = status
                )
            )
            true
        }
    }

    /** Flushes the offline remark queue. */
    suspend fun syncPendingRemarks(batchSize: Int = 50) = withContext(Dispatchers.IO) {
        val pending = remarkDao.getByState(RemarkEntity.SyncState.PENDING, batchSize) +
            remarkDao.getByState(RemarkEntity.SyncState.FAILED, batchSize)
        for (entity in pending.distinctBy { it.id }) {
            val result = safeApiCall(moshi) {
                api.saveRemark(
                    RemarkRequest(
                        deviceId = deviceInfo.deviceId,
                        clientCallId = entity.clientCallId,
                        phone = entity.phoneNumber,
                        callType = entity.callType,
                        name = entity.contactName,
                        company = entity.company,
                        remark = entity.remark,
                        status = entity.status
                    )
                )
            }
            if (result is NetworkResult.Success) {
                remarkDao.markState(entity.id, RemarkEntity.SyncState.SYNCED)
            } else {
                remarkDao.markState(entity.id, RemarkEntity.SyncState.FAILED)
            }
        }
    }
}
