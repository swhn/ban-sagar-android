package com.madebysai.bansagar.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val showNsfw: Flow<Boolean> = dataStore.data.map { it[SHOW_NSFW] ?: false }

    val themeMode: Flow<ThemeMode> = dataStore.data.map {
        when (it[THEME_MODE]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK"  -> ThemeMode.DARK
            else   -> ThemeMode.SYSTEM
        }
    }

    val wotdNotifications: Flow<Boolean> = dataStore.data.map { it[WOTD_NOTIFICATIONS] ?: true }

    suspend fun setShowNsfw(value: Boolean) { dataStore.edit { it[SHOW_NSFW] = value } }
    suspend fun setThemeMode(mode: ThemeMode) { dataStore.edit { it[THEME_MODE] = mode.name } }
    suspend fun setWotdNotifications(value: Boolean) { dataStore.edit { it[WOTD_NOTIFICATIONS] = value } }

    companion object {
        private val SHOW_NSFW          = booleanPreferencesKey("show_nsfw")
        private val THEME_MODE         = stringPreferencesKey("theme_mode")
        private val WOTD_NOTIFICATIONS = booleanPreferencesKey("wotd_notifications")
    }
}
