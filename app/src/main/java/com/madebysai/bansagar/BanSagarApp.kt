package com.madebysai.bansagar

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.madebysai.bansagar.service.BanSagarMessagingService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BanSagarApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            BanSagarMessagingService.CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
