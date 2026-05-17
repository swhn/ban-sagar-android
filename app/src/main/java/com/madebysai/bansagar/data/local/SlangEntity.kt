package com.madebysai.bansagar.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.madebysai.bansagar.data.model.Slang

@Entity(tableName = "slangs")
data class SlangEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val word: String,
    val pronunciation: String?,
    val meaning: String,
    @ColumnInfo(name = "meaning_burmese") val meaningBurmese: String?,
    val examples: List<String>,
    @ColumnInfo(name = "author_id") val authorId: String,
    @ColumnInfo(name = "author_name") val authorName: String?,
    val status: String,
    val upvotes: Int,
    val downvotes: Int,
    val views: Int,
    @ColumnInfo(name = "view_history") val viewHistory: Map<String, Int>,
    @ColumnInfo(name = "is_nsfw") val isNsfw: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis(),
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
