package com.bansagar.app.domain.repository

import com.bansagar.app.data.model.Slang
import com.bansagar.app.domain.model.Timeframe

interface SlangRepository {
    suspend fun getTrending(timeframe: Timeframe = Timeframe.Month, limit: Int = 20, offset: Int = 0): List<Slang>
    suspend fun getLatest(limit: Int = 20, offset: Int = 0): List<Slang>
    suspend fun getTop(limit: Int = 20, offset: Int = 0): List<Slang>
    suspend fun getRandom(limit: Int = 20): List<Slang>
    suspend fun search(query: String): List<Slang>
    suspend fun getBySlug(slug: String): Slang?
    suspend fun getRelated(slang: Slang, limit: Int = 6): List<Slang>
    suspend fun incrementView(slangId: String)
    // Cache-only reads for instant pre-load
    suspend fun getCachedLatest(limit: Int): List<Slang>
    suspend fun getCachedTop(limit: Int): List<Slang>
    suspend fun getCachedAll(): List<Slang>
}
