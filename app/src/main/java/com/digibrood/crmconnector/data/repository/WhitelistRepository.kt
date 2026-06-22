package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.local.dao.WhitelistDao
import com.digibrood.crmconnector.data.local.entity.WhitelistEntity
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.WhitelistItem
import com.digibrood.crmconnector.data.remote.dto.WhitelistProposeRequest
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.util.DeviceInfoProvider
import com.digibrood.crmconnector.util.PhoneUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Outcome of proposing a number, surfaced to the UI. */
sealed interface ProposeResult {
    object Added : ProposeResult            // queued locally + sent (or will retry)
    object AlreadyExists : ProposeResult
    object InvalidNumber : ProposeResult
}

/**
 * Owns the local whitelist of personal numbers the user wants excluded from CRM
 * upload, plus their admin-approval state (D7).
 *
 * Flow:
 *  1. User proposes a number -> stored locally as PENDING, POST /whitelist/propose.
 *  2. Until an admin approves it, the number stays PENDING and its calls keep
 *     uploading (we only exclude APPROVED numbers).
 *  3. GET /device/status returns a `whitelist` array; [syncFromDeviceStatus]
 *     updates each number's status. Once APPROVED, the call queue stops sending
 *     that number.
 *
 * The propose endpoint may not be live on the CRM yet; failures are tolerated and
 * unproposed entries are retried by [retryUnproposed] on each sync cycle.
 */
@Singleton
class WhitelistRepository @Inject constructor(
    private val api: CrmApiService,
    private val moshi: Moshi,
    private val dao: WhitelistDao,
    private val deviceInfo: DeviceInfoProvider
) {

    fun observe(): Flow<List<WhitelistEntity>> = dao.observeAll()

    /** Normalised numbers the admin has approved (excluded from upload). */
    suspend fun approvedNumbers(): Set<String> = withContext(Dispatchers.IO) {
        dao.approvedNumbers().map { PhoneUtils.normalize(it) }.toSet()
    }

    suspend fun isApproved(rawNumber: String?): Boolean {
        val normalized = PhoneUtils.normalize(rawNumber)
        if (normalized.isBlank()) return false
        return approvedNumbers().contains(normalized)
    }

    /** Adds a proposed number locally and tries to notify the CRM admin. */
    suspend fun propose(rawNumber: String, label: String?): ProposeResult = withContext(Dispatchers.IO) {
        val number = PhoneUtils.normalize(rawNumber)
        if (!PhoneUtils.isValid(number)) return@withContext ProposeResult.InvalidNumber
        if (dao.getByNumber(number) != null) return@withContext ProposeResult.AlreadyExists

        dao.insert(
            WhitelistEntity(
                number = number,
                label = label?.trim()?.takeIf { it.isNotEmpty() },
                status = WhitelistEntity.Status.PENDING,
                proposedToCrm = false
            )
        )
        sendPropose(number, label)
        ProposeResult.Added
    }

    /** Removes a number from the local whitelist (re-enables its uploads locally). */
    suspend fun remove(rawNumber: String) = withContext(Dispatchers.IO) {
        dao.deleteByNumber(PhoneUtils.normalize(rawNumber))
    }

    /** Re-attempts proposing any entries that never reached the CRM. */
    suspend fun retryUnproposed() = withContext(Dispatchers.IO) {
        dao.getUnproposed().forEach { entry -> sendPropose(entry.number, entry.label) }
    }

    /** Applies the admin's decisions from the device/status `whitelist` array. */
    suspend fun syncFromDeviceStatus(items: List<WhitelistItem>?) = withContext(Dispatchers.IO) {
        if (items == null) return@withContext
        val now = System.currentTimeMillis()
        for (item in items) {
            val number = PhoneUtils.normalize(item.number)
            if (number.isBlank()) continue
            val status = mapStatus(item.status)
            val existing = dao.getByNumber(number)
            if (existing == null) {
                // Admin added it directly in the CRM — reflect it locally.
                dao.insert(
                    WhitelistEntity(
                        number = number,
                        status = status,
                        proposedToCrm = true,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } else {
                dao.markProposed(number, proposed = true, status = status, now = now)
            }
        }
    }

    private suspend fun sendPropose(number: String, label: String?) {
        val result = safeApiCall(moshi) {
            api.proposeWhitelist(
                WhitelistProposeRequest(
                    deviceId = deviceInfo.deviceId,
                    number = number,
                    label = label
                )
            )
        }
        if (result is NetworkResult.Success) {
            val status = mapStatus(result.data.status)
            dao.markProposed(number, proposed = true, status = status, now = System.currentTimeMillis())
        }
        // On failure (e.g. endpoint not live yet) keep proposedToCrm=false so the
        // next sync retries; the number stays PENDING and its calls keep uploading.
    }

    private fun mapStatus(raw: String?): String = when (raw?.lowercase()) {
        "approved", "active", "whitelisted" -> WhitelistEntity.Status.APPROVED
        "rejected", "denied" -> WhitelistEntity.Status.REJECTED
        else -> WhitelistEntity.Status.PENDING
    }
}
