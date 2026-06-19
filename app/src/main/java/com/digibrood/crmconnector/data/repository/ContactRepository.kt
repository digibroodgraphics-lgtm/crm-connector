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
     * Looks up a phone number against the CRM. Falls back to the local contact
     * name when the CRM cannot be reached, so the popup always shows something
     * useful.
     */
    suspend fun lookup(phoneNumber: String): ContactLookupResponse = withContext(Dispatchers.IO) {
        val normalized = PhoneUtils.normalize(phoneNumber)
        when (val result = safeApiCall(moshi) { api.lookupContact(normalized) }) {
            is NetworkResult.Success -> {
                val body = result.data
                if (body.found || !body.contactName.isNullOrBlank()) {
                    body
                } else {
                    body.copy(contactName = contactReader.displayNameFor(normalized))
                }
            }
            else -> ContactLookupResponse(
                found = false,
                contactName = contactReader.displayNameFor(normalized),
                status = null
            )
        }
    }

    /**
     * Sends a remark to the CRM. If the call fails, the remark is queued for a
     * later retry and the method still reports success to the user.
     */
    suspend fun saveRemark(
        clientCallId: String?,
        phoneNumber: String,
        contactName: String?,
        remark: String,
        status: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val normalized = PhoneUtils.normalize(phoneNumber)
        val request = RemarkRequest(
            deviceId = deviceInfo.deviceId,
            clientCallId = clientCallId,
            phone = normalized,
            contactName = contactName?.takeIf { it.isNotBlank() },
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
                    contactName = contactName,
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
                        contactName = entity.contactName,
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
