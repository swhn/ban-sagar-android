package com.bansagar.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val showNsfw: Flow<Boolean> = dataStore.data.map { it[SHOW_NSFW] ?: false }

    suspend fun setShowNsfw(value: Boolean) {
        dataStore.edit { it[SHOW_NSFW] = value }
    }

    companion object {
        private val SHOW_NSFW = booleanPreferencesKey("show_nsfw")
    }
}
