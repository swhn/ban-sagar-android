package com.bansagar.app.domain.repository

import com.bansagar.app.data.model.AppUser
import com.bansagar.app.data.model.UserStats

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
}
