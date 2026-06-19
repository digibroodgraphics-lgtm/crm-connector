package com.digibrood.crmconnector.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.digibrood.crmconnector.sync.SyncController
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodically pings the CRM so the backend knows the device is alive and so a
 * status change (approval, revocation) is picked up promptly.
 */
@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncController: SyncController
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            syncController.runHeartbeat()
            Result.success()
        } catch (t: Throwable) {
            Result.success()
        }
    }
}
