package com.digibrood.crmconnector.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.digibrood.crmconnector.sync.SyncController
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Uploads the pending recording queue (presign -> R2 PUT -> confirm). Retries
 * automatically on failure via the work request's exponential backoff.
 */
@HiltWorker
class RecordingUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncController: SyncController
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            if (syncController.runRecordingUpload()) Result.success() else Result.retry()
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}
