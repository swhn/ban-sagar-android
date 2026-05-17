package com.madebysai.bansagar.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.madebysai.bansagar.MainActivity
import com.madebysai.bansagar.R
import com.madebysai.bansagar.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BanSagarMessagingService : FirebaseMessagingService() {

    @Inject lateinit var client: SupabaseClient
    @Inject lateinit var userRepository: UserRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            try {
                val userId = client.auth.currentUserOrNull()?.id ?: return@launch
                userRepository.updateFcmToken(userId, token)
            } catch (_: Exception) { }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val notification = message.notification ?: return
        val type = message.data["type"]
        val channelId = if (type == "word_of_the_day") WOTD_CHANNEL_ID else CHANNEL_ID
        val slug = message.data["slug"]
        showNotification(
            title = notification.title,
            body = notification.body,
            tag = message.messageId,
            channelId = channelId,
            slug = slug,
        )
    }

    private fun showNotification(
        title: String?,
        body: String?,
        tag: String?,
        channelId: String = CHANNEL_ID,
        slug: String? = null,
    ) {
        if (title == null && body == null) return

        val intent = if (!slug.isNullOrBlank()) {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://bansagar.com/slang/$slug")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            slug?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(tag, 0, notification)
    }

    companion object {
        const val CHANNEL_ID = "ban_sagar_notifications"
        const val WOTD_CHANNEL_ID = "word_of_the_day"
    }
}
