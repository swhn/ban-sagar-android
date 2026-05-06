package com.bansagar.app.data.repository

import com.bansagar.app.data.model.Slang
import com.bansagar.app.data.model.SlangSubmission
import com.bansagar.app.domain.repository.ContributeRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class ContributeRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
) : ContributeRepository {

    override suspend fun submitSlang(submission: SlangSubmission): Slang {
        return client.from("slangs").insert(submission) {
            select()
        }.decodeSingle()
    }

    override suspend fun getUserHistory(userId: String): List<Slang> {
        return client.from("slangs").select {
            filter { eq("author_id", userId) }
            order("created_at", Order.DESCENDING)
        }.decodeList()
    }

    override suspend fun checkDuplicates(word: String): List<Slang> {
        return try {
            client.from("slangs").select {
                filter { ilike("word", "%$word%") }
                limit(5)
            }.decodeList()
        } catch (_: Exception) { emptyList() }
    }

    // Conservative default — all user submissions go to pending.
    // Moderator/admin role is checked in AddSlangViewModel before calling this.
    override suspend fun getRequireApproval(): Boolean = true
}
