package com.madebysai.bansagar.domain.repository

import com.madebysai.bansagar.data.model.ContributorStats

interface LeaderboardRepository {
    suspend fun getContributors(): List<ContributorStats>
}
