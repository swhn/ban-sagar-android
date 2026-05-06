package com.bansagar.app.domain.repository

interface VoteRepository {
    suspend fun getUserVote(userId: String, slangId: String): String?
    suspend fun castVote(userId: String, slangId: String, voteType: String)
}
