package com.bansagar.app.data.model

data class ContributorStats(
    val authorId: String,
    val authorName: String,
    val avatarUrl: String?,
    val approvedCount: Int,
    val totalCount: Int,
    val totalUpvotes: Int,
    val totalViews: Int,
)
