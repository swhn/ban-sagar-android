package com.madebysai.bansagar.data.repository

import com.madebysai.bansagar.data.model.Slang
import com.madebysai.bansagar.data.model.SlangSubmission
import com.madebysai.bansagar.domain.repository.ContributeRepository
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

    override suspend fun getRequireApproval(): Boolean = true
}
