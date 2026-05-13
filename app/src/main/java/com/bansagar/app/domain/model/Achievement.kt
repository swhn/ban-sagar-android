package com.bansagar.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import com.bansagar.app.data.model.ContributorStats

enum class AchievementTier { BRONZE, SILVER, GOLD, LEGENDARY }

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tier: AchievementTier,
    val check: (ContributorStats) -> Boolean,
)

val ACHIEVEMENTS: List<Achievement> = listOf(
    // Bronze
    Achievement("first_word", "First Word", "Submit your first slang", Icons.Outlined.MenuBook, AchievementTier.BRONZE) { it.totalCount >= 1 },
    Achievement("getting_started", "Getting Started", "3 slangs approved", Icons.Outlined.Lightbulb, AchievementTier.BRONZE) { it.approvedCount >= 3 },
    Achievement("wordsmith", "Wordsmith", "5 slangs approved", Icons.Outlined.Star, AchievementTier.BRONZE) { it.approvedCount >= 5 },
    Achievement("crowd_favorite", "Crowd Favorite", "10 total upvotes", Icons.Outlined.Favorite, AchievementTier.BRONZE) { it.totalUpvotes >= 10 },
    Achievement("first_glance", "First Glance", "50 total views", Icons.Outlined.Visibility, AchievementTier.BRONZE) { it.totalViews >= 50 },
    // Silver
    Achievement("community_voice", "Community Voice", "25 total upvotes", Icons.Outlined.Group, AchievementTier.SILVER) { it.totalUpvotes >= 25 },
    Achievement("contributor", "Active Contributor", "10 slangs submitted", Icons.Outlined.ChatBubbleOutline, AchievementTier.SILVER) { it.totalCount >= 10 },
    Achievement("slang_scholar", "Slang Scholar", "15 slangs approved", Icons.Outlined.MilitaryTech, AchievementTier.SILVER) { it.approvedCount >= 15 },
    Achievement("trending", "Trending", "50 total upvotes", Icons.AutoMirrored.Outlined.TrendingUp, AchievementTier.SILVER) { it.totalUpvotes >= 50 },
    Achievement("viral", "Viral", "100+ total views", Icons.Outlined.RemoveRedEye, AchievementTier.SILVER) { it.totalViews >= 100 },
    // Gold
    Achievement("prolific", "Prolific Writer", "25 slangs submitted", Icons.Outlined.Edit, AchievementTier.GOLD) { it.totalCount >= 25 },
    Achievement("dictionary_builder", "Dictionary Builder", "30 slangs approved", Icons.Outlined.GpsFixed, AchievementTier.GOLD) { it.approvedCount >= 30 },
    Achievement("on_fire", "On Fire", "100 total upvotes", Icons.Outlined.LocalFireDepartment, AchievementTier.GOLD) { it.totalUpvotes >= 100 },
    Achievement("globe_trotter", "Globe Trotter", "500+ total views", Icons.Outlined.Public, AchievementTier.GOLD) { it.totalViews >= 500 },
    Achievement("slang_master", "Slang Master", "50 slangs approved", Icons.Outlined.WorkspacePremium, AchievementTier.GOLD) { it.approvedCount >= 50 },
    // Legendary
    Achievement("legendary", "Legendary", "100 slangs approved", Icons.Outlined.RocketLaunch, AchievementTier.LEGENDARY) { it.approvedCount >= 100 },
    Achievement("hall_of_fame", "Hall of Fame", "1000+ total views", Icons.Outlined.Bolt, AchievementTier.LEGENDARY) { it.totalViews >= 1000 },
    Achievement("diamond", "Diamond Contributor", "200 total upvotes", Icons.Outlined.Diamond, AchievementTier.LEGENDARY) { it.totalUpvotes >= 200 },
    Achievement("verified_legend", "Verified Legend", "50 slangs & 100 upvotes", Icons.Outlined.Verified, AchievementTier.LEGENDARY) { it.approvedCount >= 50 && it.totalUpvotes >= 100 },
)
