package com.bansagar.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vote(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("slang_id") val slangId: String = "",
    @SerialName("vote_type") val voteType: String = "",
    @SerialName("created_at") val createdAt: String = "",
)
