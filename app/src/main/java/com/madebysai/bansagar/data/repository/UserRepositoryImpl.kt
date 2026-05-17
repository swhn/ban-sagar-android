package com.madebysai.bansagar.data.repository

import com.madebysai.bansagar.data.model.AppUser
import com.madebysai.bansagar.data.model.Slang
import com.madebysai.bansagar.data.model.UserStats
import com.madebysai.bansagar.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
) : UserRepository {

    override suspend fun getProfile(userId: String): AppUser? {
        return try {
            client.from("users").select {
                filter { eq("id", userId) }
                limit(1)
            }.decodeSingleOrNull<AppUser>()
        } catch (_: Exception) { null }
    }

    override suspend fun updateDisplayName(userId: String, name: String) {
        client.from("users").update(
            buildJsonObject { put("display_name", name) }
        ) {
            filter { eq("id", userId) }
        }
    }

    override suspend fun updatePreferences(
        userId: String,
        showNsfw: Boolean,
        notifyApproved: Boolean,
        notifyBadges: Boolean,
    ) {
        client.from("users").update(
            buildJsonObject {
                put("show_nsfw", showNsfw)
                put("notify_approved", notifyApproved)
                put("notify_badges", notifyBadges)
            }
        ) {
            filter { eq("id", userId) }
        }
    }

    override suspend fun getStats(userId: String): UserStats {
        return try {
            val slangs = client.from("slangs").select {
                filter { eq("author_id", userId) }
            }.decodeList<Slang>()
            UserStats(
                submittedCount = slangs.size,
                approvedCount = slangs.count { it.status == "approved" },
                totalUpvotes = slangs.sumOf { it.upvotes },
            )
        } catch (_: Exception) { UserStats() }
    }

    override suspend fun updateFcmToken(userId: String, token: String) {
        try {
            client.from("users").update(
                buildJsonObject { put("fcm_token", token) }
            ) {
                filter { eq("id", userId) }
            }
        } catch (_: Exception) { }
    }
}
