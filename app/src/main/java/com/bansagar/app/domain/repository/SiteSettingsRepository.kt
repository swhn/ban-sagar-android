package com.bansagar.app.domain.repository

import com.bansagar.app.data.model.SiteSettings

interface SiteSettingsRepository {
    suspend fun getSettings(): SiteSettings
}
