package com.bansagar.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.bansagar.app.MainActivity
import com.bansagar.app.R
import com.bansagar.app.domain.repository.UserRepository
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

    /** Called when FCM assigns a new registration token (install, data-clear, token rotation). */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            try {
                val userId = client.auth.currentUserOrNull()?.id ?: return@launch
                userRepository.updateFcmToken(userId, token)
            } catch (_: Exception) { /* token will sync on next sign-in */ }
        }
    }

    /**
     * Called when a data/notification message arrives while the app is in the foreground.
     * Background messages are handled automatically by the system using the notification payload.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val notification = message.notification ?: return
        showNotification(
            title = notification.title,
            body = notification.body,
            tag = message.messageId,
        )
    }

    private fun showNotification(title: String?, body: String?, tag: String?) {
        if (title == null && body == null) return

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
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
    }
}
