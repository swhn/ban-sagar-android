package com.bansagar.app.data.repository

import com.bansagar.app.data.model.Slang
import com.bansagar.app.domain.model.Timeframe
import com.bansagar.app.domain.repository.SlangRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class SlangRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
) : SlangRepository {

    override suspend fun getTrending(
        timeframe: Timeframe,
        limit: Int,
        offset: Int,
        showNsfw: Boolean,
    ): List<Slang> {
        val all: List<Slang> = client.from("slangs").select {
            filter {
                eq("status", "approved")
                if (!showNsfw) eq("is_nsfw", false)
            }
        }.decodeList()

        val days = timeframe.days()
        val today = LocalDate.now(ZoneOffset.UTC)
        val windowDates: List<String> = (0 until days).map { i ->
            today.minusDays(i.toLong()).toString()
        }

        return all
            .map { slang -> slang to trendingScore(slang, windowDates) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
            .drop(offset)
            .take(limit)
    }

    private fun trendingScore(slang: Slang, windowDates: List<String>): Int {
        if (slang.viewHistory.isEmpty()) return 0
        var score = 0
        for (date in windowDates) {
            score += slang.viewHistory[date] ?: 0
        }
        return score
    }

    override suspend fun getLatest(limit: Int, offset: Int, showNsfw: Boolean): List<Slang> {
        return client.from("slangs").select {
            filter {
                eq("status", "approved")
                if (!showNsfw) eq("is_nsfw", false)
            }
            order("created_at", Order.DESCENDING)
            range(offset.toLong(), (offset + limit - 1).toLong())
        }.decodeList()
    }

    override suspend fun getTop(limit: Int, offset: Int, showNsfw: Boolean): List<Slang> {
        return client.from("slangs").select {
            filter {
                eq("status", "approved")
                if (!showNsfw) eq("is_nsfw", false)
            }
            order("upvotes", Order.DESCENDING)
            range(offset.toLong(), (offset + limit - 1).toLong())
        }.decodeList()
    }

    override suspend fun getRandom(limit: Int, showNsfw: Boolean): List<Slang> {
        val all: List<Slang> = client.from("slangs").select {
            filter {
                eq("status", "approved")
                if (!showNsfw) eq("is_nsfw", false)
            }
        }.decodeList()
        return all.shuffled().take(limit)
    }

    override suspend fun search(query: String, showNsfw: Boolean): List<Slang> {
        return client.from("slangs").select {
            filter {
                eq("status", "approved")
                if (!showNsfw) eq("is_nsfw", false)
                or {
                    ilike("word", "%$query%")
                    ilike("meaning", "%$query%")
                    ilike("meaning_burmese", "%$query%")
                }
            }
            limit(30)
        }.decodeList()
    }

    override suspend fun getBySlug(slug: String): Slang? {
        return try {
            client.from("slangs").select {
                filter { eq("slug", slug) }
                limit(1)
            }.decodeSingleOrNull()
        } catch (_: Exception) {
            try {
                client.from("slangs").select {
                    filter { eq("id", slug) }
                    limit(1)
                }.decodeSingleOrNull()
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun getRelated(slang: Slang, limit: Int): List<Slang> {
        val keywords = slang.meaning
            .lowercase()
            .split(Regex("\\W+"))
            .filter { it.length > 3 && it.matches(Regex("^[a-z]+$")) }
            .take(5)

        val related = mutableListOf<Slang>()

        for (keyword in keywords) {
            if (related.size >= limit) break
            try {
                val results: List<Slang> = client.from("slangs").select {
                    filter {
                        eq("status", "approved")
                        neq("id", slang.id)
                        ilike("meaning", "%$keyword%")
                    }
                    limit(limit.toLong())
                }.decodeList()
                for (item in results) {
                    if (related.none { it.id == item.id }) related.add(item)
                }
            } catch (_: Exception) { }
        }

        if (related.size < 4) {
            try {
                val popular: List<Slang> = client.from("slangs").select {
                    filter {
                        eq("status", "approved")
                        neq("id", slang.id)
                    }
                    order("upvotes", Order.DESCENDING)
                    limit(limit.toLong())
                }.decodeList()
                for (item in popular) {
                    if (related.none { it.id == item.id }) related.add(item)
                }
            } catch (_: Exception) { }
        }

        return related.take(limit)
    }

    override suspend fun incrementView(slangId: String) {
        try {
            client.postgrest.rpc("increment_view", buildJsonObject {
                put("p_slang_id", slangId)
            })
        } catch (_: Exception) { }
    }
}
