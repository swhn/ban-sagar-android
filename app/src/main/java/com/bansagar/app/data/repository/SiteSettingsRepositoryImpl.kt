package com.bansagar.app.data.repository

import com.bansagar.app.data.model.SiteSettings
import com.bansagar.app.domain.repository.SiteSettingsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class SiteSettingsRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
) : SiteSettingsRepository {

    override suspend fun getSettings(): SiteSettings {
        return try {
            client.from("site_settings").select().decodeSingle()
        } catch (_: Exception) { SiteSettings() }
    }
}
