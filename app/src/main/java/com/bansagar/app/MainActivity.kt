package com.madebysai.bansagar

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
import com.madebysai.bansagar.data.preferences.ThemeMode
import com.madebysai.bansagar.data.preferences.UserPreferencesRepository
import com.madebysai.bansagar.service.BanSagarMessagingService
import com.madebysai.bansagar.ui.navigation.AppNavigation
import com.madebysai.bansagar.ui.theme.BanSagarTheme
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: UserPreferencesRepository

    private lateinit var appUpdateManager: AppUpdateManager

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { /* flexible update cancellation is non-critical; ignored */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        createNotificationChannels()
        syncWotdSubscription()
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdate()
        setContent {
            val themeMode by prefs.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            BanSagarTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If a flexible update finished downloading while the user was in the app,
        // trigger the install confirmation overlay.
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                }
            }
        }
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) return@addOnSuccessListener

            // Priority 4+ in Play Console triggers an immediate (blocking) update.
            // Everything else uses a flexible (background download) update.
            val updateType = when {
                info.updatePriority() >= 4 && info.isImmediateUpdateAllowed -> AppUpdateType.IMMEDIATE
                info.isFlexibleUpdateAllowed -> AppUpdateType.FLEXIBLE
                else -> return@addOnSuccessListener
            }

            appUpdateManager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(updateType).build(),
            )
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
