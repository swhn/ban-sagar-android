package com.bansagar.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bansagar.app.data.model.Slang

@Entity(tableName = "slangs")
data class SlangEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val word: String,
    val pronunciation: String?,
    val meaning: String,
    val meaningBurmese: String?,
    val examples: List<String>,
    val authorId: String,
    val authorName: String?,
    val status: String,
    val upvotes: Int,
    val downvotes: Int,
    val views: Int,
    val viewHistory: Map<String, Int>,
    val isNsfw: Boolean,
    val createdAt: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

fun SlangEntity.toSlang() = Slang(
    id = id, slug = slug, word = word, pronunciation = pronunciation,
    meaning = meaning, meaningBurmese = meaningBurmese, examples = examples,
    authorId = authorId, authorName = authorName, status = status,
    upvotes = upvotes, downvotes = downvotes, views = views,
    viewHistory = viewHistory, isNsfw = isNsfw, createdAt = createdAt,
)

fun Slang.toEntity() = SlangEntity(
    id = id, slug = slug, word = word, pronunciation = pronunciation,
    meaning = meaning, meaningBurmese = meaningBurmese, examples = examples,
    authorId = authorId, authorName = authorName, status = status,
    upvotes = upvotes, downvotes = downvotes, views = views,
    viewHistory = viewHistory, isNsfw = isNsfw, createdAt = createdAt,
)
