package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.local.dao.RemarkDao
import com.digibrood.crmconnector.data.local.entity.RemarkEntity
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.ContactLookupResponse
import com.digibrood.crmconnector.data.remote.dto.RemarkRequest
import com.digibrood.crmconnector.data.remote.safeApiCall
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
    private val contactReader: ContactReader
) {

    /**
     * Looks up a phone number against the CRM, returning the contact's name and
     * company for pre-filling the popup. Falls back to the device's local contact
     * name when the CRM is unreachable or errors, so the popup is always useful.
     */
    suspend fun lookup(phoneNumber: String): ContactDetails = withContext(Dispatchers.IO) {
        val normalized = PhoneUtils.normalize(phoneNumber)
        when (val result = safeApiCall(moshi) { api.lookupContact(normalized) }) {
            is NetworkResult.Success -> {
                val body: ContactLookupResponse = result.data
                val name = body.effectiveName ?: contactReader.displayNameFor(normalized)
                ContactDetails(
                    found = body.found || !body.effectiveName.isNullOrBlank(),
                    name = name,
                    company = body.effectiveCompany,
                    status = body.effectiveStatus
                )
            }
            else -> ContactDetails(
                found = false,
                name = contactReader.displayNameFor(normalized),
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
        status: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val normalized = PhoneUtils.normalize(phoneNumber)
        val request = RemarkRequest(
            deviceId = deviceInfo.deviceId,
            clientCallId = clientCallId,
            phone = normalized,
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
