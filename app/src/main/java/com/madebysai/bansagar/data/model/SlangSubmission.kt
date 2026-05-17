package com.madebysai.bansagar.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SlangSubmission(
    val word: String,
    val slug: String,
    val pronunciation: String? = null,
    val meaning: String,
    @SerialName("meaning_burmese") val meaningBurmese: String? = null,
    val examples: List<String> = emptyList(),
    @SerialName("is_nsfw") val isNsfw: Boolean = false,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String? = null,
    val status: String = "pending",
)
