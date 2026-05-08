package com.bansagar.app.data.repository

import com.bansagar.app.data.model.ContributorStats
import com.bansagar.app.domain.repository.LeaderboardRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class SlangRow(
    @SerialName("author_id") val authorId: String = "",
    @SerialName("author_name") val authorName: String = "",
    val status: String = "",
    val upvotes: Int = 0,
    val views: Int = 0,
)

@Serializable
private data class UserRow(
    val id: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

class LeaderboardRepositoryImpl(
    private val client: SupabaseClient,
) : LeaderboardRepository {

    override suspend fun getContributors(): List<ContributorStats> {
        val slangs = client.from("slangs").select().decodeList<SlangRow>()
        val users = client.from("users").select().decodeList<UserRow>()

        val avatarMap = users.associate { it.id to it.avatarUrl }
        val statsMap = mutableMapOf<String, ContributorStats>()

        slangs.filter { it.authorId.isNotEmpty() }.forEach { s ->
            val existing = statsMap[s.authorId]
            if (existing != null) {
                statsMap[s.authorId] = existing.copy(
                    totalCount = existing.totalCount + 1,
                    approvedCount = if (s.status == "approved") existing.approvedCount + 1 else existing.approvedCount,
                    totalUpvotes = existing.totalUpvotes + s.upvotes,
                    totalViews = existing.totalViews + s.views,
                )
            } else {
                statsMap[s.authorId] = ContributorStats(
                    authorId = s.authorId,
                    authorName = s.authorName.ifEmpty { "Anonymous" },
                    avatarUrl = avatarMap[s.authorId],
                    approvedCount = if (s.status == "approved") 1 else 0,
                    totalCount = 1,
                    totalUpvotes = s.upvotes,
                    totalViews = s.views,
                )
            }
        }

        return statsMap.values
            .sortedWith(compareByDescending<ContributorStats> { it.approvedCount }
                .thenByDescending { it.totalUpvotes })
    }
}
