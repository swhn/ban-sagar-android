package com.bansagar.app.data.repository

import com.bansagar.app.data.local.SlangDao
import com.bansagar.app.data.local.toEntity
import com.bansagar.app.data.local.toSlang
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

class SlangRepositoryImpl(
    private val client: SupabaseClient,
    private val dao: SlangDao,
) : SlangRepository {

    override suspend fun getTrending(timeframe: Timeframe, limit: Int, offset: Int): List<Slang> {
        return try {
            val all: List<Slang> = client.from("slangs").select {
                filter { eq("status", "approved") }
            }.decodeList()
            dao.insertAll(all.map { it.toEntity() })
            val days = timeframe.days()
            val today = LocalDate.now(ZoneOffset.UTC)
            val windowDates = (0 until days).map { i -> today.minusDays(i.toLong()).toString() }
            all.map { slang -> slang to trendingScore(slang, windowDates) }
                .sortedWith(
                    compareByDescending<Pair<Slang, Int>> { it.second }
                        .thenByDescending { it.first.upvotes }
                )
                .map { it.first }
                .drop(offset)
                .take(limit)
        } catch (_: Exception) {
            dao.getTop(limit, offset).map { it.toSlang() }
        }
    }

    private fun trendingScore(slang: Slang, windowDates: List<String>): Int {
        if (slang.viewHistory.isEmpty()) return 0
        return windowDates.sumOf { slang.viewHistory[it] ?: 0 }
    }

    override suspend fun getLatest(limit: Int, offset: Int): List<Slang> {
        return try {
            val results: List<Slang> = client.from("slangs").select {
                filter { eq("status", "approved") }
                order("created_at", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }.decodeList()
            if (offset == 0) dao.insertAll(results.map { it.toEntity() })
            results
        } catch (_: Exception) {
            dao.getLatest(limit, offset).map { it.toSlang() }
        }
    }

    override suspend fun getTop(limit: Int, offset: Int): List<Slang> {
        return try {
            val results: List<Slang> = client.from("slangs").select {
                filter { eq("status", "approved") }
                order("upvotes", Order.DESCENDING)
                range(offset.toLong(), (offset + limit - 1).toLong())
            }.decodeList()
            if (offset == 0) dao.insertAll(results.map { it.toEntity() })
            results
        } catch (_: Exception) {
            dao.getTop(limit, offset).map { it.toSlang() }
        }
    }

    override suspend fun getRandom(limit: Int): List<Slang> {
        return try {
            val all: List<Slang> = client.from("slangs").select {
                filter { eq("status", "approved") }
            }.decodeList()
            dao.insertAll(all.map { it.toEntity() })
            all.shuffled().take(limit)
        } catch (_: Exception) {
            dao.getAll().map { it.toSlang() }.shuffled().take(limit)
        }
    }

    override suspend fun search(query: String): List<Slang> {
        return client.from("slangs").select {
            filter {
                eq("status", "approved")
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
            val result = client.from("slangs").select {
                filter { eq("slug", slug) }
                limit(1)
            }.decodeSingleOrNull<Slang>()
            if (result != null) {
                dao.insert(result.toEntity())
                result
            } else {
                val byId = client.from("slangs").select {
                    filter { eq("id", slug) }
                    limit(1)
                }.decodeSingleOrNull<Slang>()
                byId?.also { dao.insert(it.toEntity()) }
            }
        } catch (_: Exception) {
            dao.getBySlug(slug)?.toSlang() ?: dao.getById(slug)?.toSlang()
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

    override suspend fun getCachedLatest(limit: Int): List<Slang> =
        dao.getLatest(limit, 0).map { it.toSlang() }

    override suspend fun getCachedTop(limit: Int): List<Slang> =
        dao.getTop(limit, 0).map { it.toSlang() }

    override suspend fun getCachedAll(): List<Slang> =
        dao.getAll().map { it.toSlang() }
}
