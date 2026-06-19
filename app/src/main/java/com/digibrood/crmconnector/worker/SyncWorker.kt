package com.digibrood.crmconnector.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.digibrood.crmconnector.sync.SyncController
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Runs a full sync cycle (status, settings, calls, recordings, remarks).
 * Used both periodically and on-demand after a call ends. Retries with the
 * exponential backoff configured on the work request.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncController: SyncController
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            if (syncController.runFullSync()) Result.success() else Result.retry()
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}
