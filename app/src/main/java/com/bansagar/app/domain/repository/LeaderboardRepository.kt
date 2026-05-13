package com.bansagar.app.domain.repository

import com.bansagar.app.data.model.ContributorStats

interface LeaderboardRepository {
    suspend fun getContributors(): List<ContributorStats>
}
