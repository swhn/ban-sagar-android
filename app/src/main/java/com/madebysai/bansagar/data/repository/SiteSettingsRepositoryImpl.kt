package com.madebysai.bansagar.data.repository

import com.madebysai.bansagar.data.model.SiteSettings
import com.madebysai.bansagar.domain.repository.SiteSettingsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
private data class KeyValue(val key: String, val value: String)

class SiteSettingsRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
) : SiteSettingsRepository {

    override suspend fun getSettings(): SiteSettings {
        return try {
            val rows = client.from("site_settings")
                .select()
                .decodeList<KeyValue>()

            val map = rows.associate { it.key to it.value }

            SiteSettings(
                allowRegistrations = map["allow_registrations"]?.toBoolean() ?: true,
                requireApproval = map["require_approval"]?.toBoolean() ?: true,
                maxSubmissionsPerDay = map["max_submissions_per_day"]?.toIntOrNull() ?: 5,
                allowNsfw = map["allow_nsfw"]?.toBoolean() ?: true,
                showRanking = map["show_ranking"]?.toBoolean() ?: true,
                siteAnnouncement = map["site_announcement"]?.takeIf { it.isNotBlank() },
            )
        } catch (_: Exception) {
            SiteSettings()
        }
    }
}
