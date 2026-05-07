package com.bansagar.app.domain.repository

import com.bansagar.app.data.model.Slang

interface SlangRepository {
    suspend fun getTrending(limit: Int = 20, offset: Int = 0, showNsfw: Boolean = false): List<Slang>
    suspend fun getLatest(limit: Int = 20, offset: Int = 0, showNsfw: Boolean = false): List<Slang>
    suspend fun getTop(limit: Int = 20, offset: Int = 0, showNsfw: Boolean = false): List<Slang>
    suspend fun getRandom(limit: Int = 20, showNsfw: Boolean = false): List<Slang>
    suspend fun search(query: String, showNsfw: Boolean = false): List<Slang>
    suspend fun getBySlug(slug: String): Slang?
    suspend fun getRelated(slang: Slang, limit: Int = 6): List<Slang>
    suspend fun incrementView(slangId: String)
}
