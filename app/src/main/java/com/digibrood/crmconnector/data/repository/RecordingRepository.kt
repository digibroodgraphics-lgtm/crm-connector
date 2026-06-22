package com.digibrood.crmconnector.data.repository

import com.digibrood.crmconnector.data.local.dao.RecordingDao
import com.digibrood.crmconnector.data.local.entity.RecordingEntity
import com.digibrood.crmconnector.data.prefs.SecurePrefs
import com.digibrood.crmconnector.data.remote.NetworkResult
import com.digibrood.crmconnector.data.remote.api.CrmApiService
import com.digibrood.crmconnector.data.remote.dto.ConfirmRequest
import com.digibrood.crmconnector.data.remote.dto.PresignRequest
import com.digibrood.crmconnector.data.remote.safeApiCall
import com.digibrood.crmconnector.di.UploadClient
import com.digibrood.crmconnector.util.Constants
import com.digibrood.crmconnector.util.DeviceInfoProvider
import com.digibrood.crmconnector.util.RecordingScanner
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Discovers call-recording files, queues them, and uploads them through the
 * three-step flow: presign -> direct PUT to R2 -> confirm. Honours the 50 MB cap
 * and the CRM-supplied recording-path override, and retries failed uploads.
 */
@Singleton
class RecordingRepository @Inject constructor(
    private val api: CrmApiService,
    private val moshi: Moshi,
    private val recordingDao: RecordingDao,
    private val scanner: RecordingScanner,
    private val prefs: SecurePrefs,
    private val deviceInfo: DeviceInfoProvider,
    @UploadClient private val uploadClient: OkHttpClient
) {

    fun pendingCountFlow(): Flow<Int> = recordingDao.pendingCount()

    suspend fun uploadedTodayCount(since: Long): Int = recordingDao.uploadedSince(since)

    /** True if a recording row already exists for this call. */
    suspend fun hasRecordingForCall(clientCallId: String): Boolean = withContext(Dispatchers.IO) {
        recordingDao.getByCall(clientCallId) != null
    }

    /** Number of call-recording files discoverable on the device since activation (diagnostics). */
    suspend fun recordingFilesOnPhone(): Int = withContext(Dispatchers.IO) {
        runCatching { scanner.scan(extraScanPaths(), prefs.activatedAtEpochMs).size }.getOrDefault(0)
    }

    private fun extraScanPaths(): List<String> {
        val override = prefs.recordingPathOverride
        return if (!override.isNullOrBlank()) listOf(override) else emptyList()
    }

    /**
     * Looks for a recording belonging to a call and queues it.
     * @return true if an uploadable recording (<= 50 MB) was found/queued.
     */
    suspend fun discoverForCall(clientCallId: String, phone: String?, callStart: Long, callEnd: Long): Boolean =
        withContext(Dispatchers.IO) {
            val file = scanner.findForCall(callStart, callEnd, extraScanPaths()) ?: return@withContext false

            if (recordingDao.existsByPath(file.path)) return@withContext file.sizeBytes <= Constants.MAX_RECORDING_BYTES

            val uploadable = file.sizeBytes in 1..Constants.MAX_RECORDING_BYTES
            recordingDao.insert(
                RecordingEntity(
                    clientCallId = clientCallId,
                    phoneNumber = phone,
                    filePath = file.path,
                    fileName = file.name,
                    mimeType = file.mimeType,
                    fileSize = file.sizeBytes,
                    recordedAt = file.lastModified,
                    uploadState = if (uploadable) RecordingEntity.UploadState.PENDING
                    else RecordingEntity.UploadState.SKIPPED
                )
            )
            uploadable
        }

    /**
     * Uploads all pending/failed recordings. Returns true if everything that was
     * attempted succeeded (so the worker can decide whether to retry).
     */
    suspend fun uploadPending(batchSize: Int = 5): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val pending = recordingDao.getByState(RecordingEntity.UploadState.PENDING, batchSize) +
            recordingDao.getByState(RecordingEntity.UploadState.FAILED, batchSize)

        if (pending.isEmpty()) return@withContext true

        var allOk = true
        for (rec in pending.distinctBy { it.id }) {
            val ok = uploadOne(rec, now)
            if (!ok) allOk = false
        }
        allOk
    }

    private suspend fun uploadOne(rec: RecordingEntity, now: Long): Boolean {
        val file = File(rec.filePath)
        if (!file.exists() || file.length() <= 0L) {
            recordingDao.markState(rec.id, RecordingEntity.UploadState.SKIPPED, now)
            return true
        }
        if (file.length() > Constants.MAX_RECORDING_BYTES) {
            recordingDao.markState(rec.id, RecordingEntity.UploadState.SKIPPED, now)
            return true
        }

        recordingDao.markState(rec.id, RecordingEntity.UploadState.UPLOADING, now)

        // Step 1: presign
        val presign = safeApiCall(moshi) {
            api.presignRecording(
                PresignRequest(
                    deviceId = deviceInfo.deviceId,
                    clientCallId = rec.clientCallId,
                    fileName = rec.fileName,
                    mimeType = rec.mimeType,
                    fileSize = file.length()
                )
            )
        }
        val presignBody = (presign as? NetworkResult.Success)?.data
        val uploadUrl = presignBody?.uploadUrl
        if (uploadUrl.isNullOrBlank()) {
            recordingDao.markState(rec.id, RecordingEntity.UploadState.FAILED, now)
            return false
        }

        // Step 2: direct upload to R2 (presigned PUT)
        val uploaded = putFile(uploadUrl, presignBody.method ?: "PUT", presignBody.headers, file, rec.mimeType)
        if (!uploaded) {
            recordingDao.markState(rec.id, RecordingEntity.UploadState.FAILED, now)
            return false
        }

        // Step 3: confirm (only after a successful PUT, success=true)
        val confirm = safeApiCall(moshi) {
            api.confirmRecording(
                ConfirmRequest(
                    deviceId = deviceInfo.deviceId,
                    recordingId = presignBody.recordingId,
                    objectKey = presignBody.objectKey,
                    clientCallId = rec.clientCallId,
                    phone = rec.phoneNumber,
                    fileSize = file.length(),
                    success = true
                )
            )
        }
        return if (confirm is NetworkResult.Success && confirm.data.ok) {
            recordingDao.markUploaded(rec.id, presignBody.recordingId, presignBody.objectKey)
            true
        } else {
            recordingDao.markState(rec.id, RecordingEntity.UploadState.FAILED, now)
            false
        }
    }

    private fun putFile(
        url: String,
        method: String,
        headers: Map<String, String>?,
        file: File,
        mimeType: String
    ): Boolean {
        return try {
            val mediaType = mimeType.toMediaTypeOrNull()
            val body = file.asRequestBody(mediaType)
            val builder = Request.Builder().url(url)
            when (method.uppercase()) {
                "POST" -> builder.post(body)
                else -> builder.put(body)
            }
            headers?.forEach { (k, v) -> builder.header(k, v) }
            uploadClient.newCall(builder.build()).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }
}
