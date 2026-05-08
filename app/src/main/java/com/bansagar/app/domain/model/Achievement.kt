package com.bansagar.app.domain.model

import com.bansagar.app.data.model.ContributorStats

enum class AchievementTier { BRONZE, SILVER, GOLD, LEGENDARY }

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val tier: AchievementTier,
    val check: (ContributorStats) -> Boolean,
)

val ACHIEVEMENTS: List<Achievement> = listOf(
    // Bronze
    Achievement("first_word", "First Word", "Submit your first slang", "📖", AchievementTier.BRONZE) { it.totalCount >= 1 },
    Achievement("getting_started", "Getting Started", "3 slangs approved", "💡", AchievementTier.BRONZE) { it.approvedCount >= 3 },
    Achievement("wordsmith", "Wordsmith", "5 slangs approved", "⭐", AchievementTier.BRONZE) { it.approvedCount >= 5 },
    Achievement("crowd_favorite", "Crowd Favorite", "10 total upvotes", "❤️", AchievementTier.BRONZE) { it.totalUpvotes >= 10 },
    Achievement("first_glance", "First Glance", "50 total views", "👁️", AchievementTier.BRONZE) { it.totalViews >= 50 },
    // Silver
    Achievement("community_voice", "Community Voice", "25 total upvotes", "👥", AchievementTier.SILVER) { it.totalUpvotes >= 25 },
    Achievement("contributor", "Active Contributor", "10 slangs submitted", "💬", AchievementTier.SILVER) { it.totalCount >= 10 },
    Achievement("slang_scholar", "Slang Scholar", "15 slangs approved", "🎖️", AchievementTier.SILVER) { it.approvedCount >= 15 },
    Achievement("trending", "Trending", "50 total upvotes", "📈", AchievementTier.SILVER) { it.totalUpvotes >= 50 },
    Achievement("viral", "Viral", "100+ total views", "🌐", AchievementTier.SILVER) { it.totalViews >= 100 },
    // Gold
    Achievement("prolific", "Prolific Writer", "25 slangs submitted", "✏️", AchievementTier.GOLD) { it.totalCount >= 25 },
    Achievement("dictionary_builder", "Dictionary Builder", "30 slangs approved", "🎯", AchievementTier.GOLD) { it.approvedCount >= 30 },
    Achievement("on_fire", "On Fire", "100 total upvotes", "🔥", AchievementTier.GOLD) { it.totalUpvotes >= 100 },
    Achievement("globe_trotter", "Globe Trotter", "500+ total views", "🌍", AchievementTier.GOLD) { it.totalViews >= 500 },
    Achievement("slang_master", "Slang Master", "50 slangs approved", "👑", AchievementTier.GOLD) { it.approvedCount >= 50 },
    // Legendary
    Achievement("legendary", "Legendary", "100 slangs approved", "🚀", AchievementTier.LEGENDARY) { it.approvedCount >= 100 },
    Achievement("hall_of_fame", "Hall of Fame", "1000+ total views", "⚡", AchievementTier.LEGENDARY) { it.totalViews >= 1000 },
    Achievement("diamond", "Diamond Contributor", "200 total upvotes", "💎", AchievementTier.LEGENDARY) { it.totalUpvotes >= 200 },
    Achievement("verified_legend", "Verified Legend", "50 slangs & 100 upvotes", "✅", AchievementTier.LEGENDARY) { it.approvedCount >= 50 && it.totalUpvotes >= 100 },
)
