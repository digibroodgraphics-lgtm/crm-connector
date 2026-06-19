package com.digibrood.crmconnector

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.digibrood.crmconnector.util.Constants
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point.
 *
 * - Bootstraps Hilt for dependency injection.
 * - Provides the WorkManager [Configuration] backed by Hilt's [HiltWorkerFactory]
 *   so workers can have dependencies injected.
 * - Registers the notification channel used by the foreground sync service.
 */
@HiltAndroidApp
class CrmConnectorApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            Constants.SYNC_CHANNEL_ID,
            getString(R.string.sync_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.sync_notification_channel_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }
}
