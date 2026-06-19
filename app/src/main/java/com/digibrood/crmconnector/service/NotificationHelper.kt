package com.digibrood.crmconnector.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.digibrood.crmconnector.MainActivity
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Builds the notifications shown by the foreground sync service and uploads. */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun buildSyncNotification(text: String? = null): Notification =
        NotificationCompat.Builder(context, Constants.SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.sync_notification_title))
            .setContentText(text ?: context.getString(R.string.sync_notification_idle))
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent())
            .build()

    fun buildUploadNotification(text: String): Notification =
        NotificationCompat.Builder(context, Constants.SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.upload_notification_title))
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
}
