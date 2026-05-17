package com.madebysai.bansagar.domain.repository

import com.madebysai.bansagar.data.model.AppUser
import com.madebysai.bansagar.data.model.UserStats

interface UserRepository {
    suspend fun getProfile(userId: String): AppUser?
    suspend fun updateDisplayName(userId: String, name: String)
    suspend fun updatePreferences(
        userId: String,
        showNsfw: Boolean,
        notifyApproved: Boolean,
        notifyBadges: Boolean,
    )
    suspend fun getStats(userId: String): UserStats
    suspend fun updateFcmToken(userId: String, token: String)
}
