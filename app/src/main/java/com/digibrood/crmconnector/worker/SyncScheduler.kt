package com.digibrood.crmconnector.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.digibrood.crmconnector.data.repository.SettingsRepository
import com.digibrood.crmconnector.util.Constants
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central place that enqueues and cancels all background work. Every request is
 * constrained to require network connectivity and uses exponential backoff so
 * offline periods and transient failures are retried gracefully.
 */
@Singleton
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val settingsRepository: SettingsRepository
) {

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Periodic full sync. Interval honours CRM-supplied settings (min 15 min). */
    fun schedulePeriodicSync() {
        val interval = settingsRepository.syncIntervalMinutes
            .coerceAtLeast(Constants.DEFAULT_SYNC_INTERVAL_MINUTES)

        val request = PeriodicWorkRequestBuilder<SyncWorker>(interval, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(Constants.WORK_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_PERIODIC_SYNC,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** Periodic heartbeat so approval/revocation is detected promptly. */
    fun scheduleHeartbeat() {
        val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(
            Constants.DEFAULT_HEARTBEAT_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(Constants.WORK_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_HEARTBEAT,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Immediate one-off sync, e.g. right after a call ends. */
    fun requestImmediateSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .addTag(Constants.WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(
            Constants.WORK_ONE_TIME_SYNC,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Immediate one-off recording upload. */
    fun requestRecordingUpload() {
        val request = OneTimeWorkRequestBuilder<RecordingUploadWorker>()
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(Constants.WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(
            Constants.WORK_RECORDING_UPLOAD,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Delayed recording-upload passes. Phones finalise the recording file a few
     * seconds (sometimes more) after a call ends, so we re-run the upload (which
     * backfills late-written recordings) a short while after the call rather than
     * waiting for the next periodic cycle.
     */
    fun scheduleDelayedRecordingUpload(delaysSeconds: List<Long> = listOf(60L, 180L)) {
        delaysSeconds.forEach { delay ->
            val request = OneTimeWorkRequestBuilder<RecordingUploadWorker>()
                .setConstraints(networkConstraint)
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(Constants.WORK_TAG)
                .build()

            workManager.enqueueUniqueWork(
                "${Constants.WORK_RECORDING_UPLOAD}_$delay",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    /** Starts periodic work. Called when the device becomes approved. */
    fun startAll() {
        schedulePeriodicSync()
        scheduleHeartbeat()
        requestImmediateSync()
    }

    /** Cancels every scheduled and running job. Called on denied/revoked/inactive. */
    fun cancelAll() {
        workManager.cancelAllWorkByTag(Constants.WORK_TAG)
        workManager.cancelUniqueWork(Constants.WORK_PERIODIC_SYNC)
        workManager.cancelUniqueWork(Constants.WORK_HEARTBEAT)
        workManager.cancelUniqueWork(Constants.WORK_ONE_TIME_SYNC)
        workManager.cancelUniqueWork(Constants.WORK_RECORDING_UPLOAD)
    }
}
