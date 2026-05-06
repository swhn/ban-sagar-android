package com.bansagar.app.data.repository

import com.bansagar.app.data.model.Vote
import com.bansagar.app.domain.repository.VoteRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class VoteRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
) : VoteRepository {

    override suspend fun getUserVote(userId: String, slangId: String): String? {
        return try {
            client.from("votes").select {
                filter {
                    eq("user_id", userId)
                    eq("slang_id", slangId)
                }
                limit(1)
            }.decodeSingleOrNull<Vote>()?.voteType
        } catch (_: Exception) { null }
    }

    override suspend fun castVote(userId: String, slangId: String, voteType: String) {
        client.postgrest.rpc("handle_vote", buildJsonObject {
            put("p_user_id", userId)
            put("p_slang_id", slangId)
            put("p_vote_type", voteType)
        })
    }
}
