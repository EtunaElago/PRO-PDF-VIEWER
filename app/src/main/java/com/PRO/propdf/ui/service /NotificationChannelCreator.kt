package com.PRO.propdf.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import com.PRO.propdf.R

object NotificationChannelCreator {

    private const val CHANNEL_ID = "thumbnail_service_channel"
    private const val CHANNEL_NAME = "Thumbnail Generation"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.thumbnail_service_channel_description)
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }

            val notificationManager = ContextCompat.getSystemService(
                context, 
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun getChannelId(): String = CHANNEL_ID
}