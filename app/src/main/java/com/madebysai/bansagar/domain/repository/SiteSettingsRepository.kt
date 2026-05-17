package com.madebysai.bansagar.domain.repository

import com.madebysai.bansagar.data.model.SiteSettings

interface SiteSettingsRepository {
    suspend fun getSettings(): SiteSettings
}
