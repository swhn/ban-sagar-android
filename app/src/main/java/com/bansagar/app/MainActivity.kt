package com.bansagar.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bansagar.app.data.preferences.ThemeMode
import com.bansagar.app.data.preferences.UserPreferencesRepository
import com.bansagar.app.service.BanSagarMessagingService
import com.bansagar.app.ui.navigation.AppNavigation
import com.bansagar.app.ui.theme.BanSagarTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: UserPreferencesRepository

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        createNotificationChannels()
        syncWotdSubscription()
        setContent {
            val themeMode by prefs.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            BanSagarTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                BanSagarMessagingService.WOTD_CHANNEL_ID,
                getString(R.string.wotd_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = getString(R.string.wotd_channel_desc) }
        )
    }

    private fun syncWotdSubscription() {
        lifecycleScope.launch {
            val enabled = prefs.wotdNotifications.first()
            val topic = BanSagarMessagingService.WOTD_CHANNEL_ID
            if (enabled) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            }
        }
    }
}
