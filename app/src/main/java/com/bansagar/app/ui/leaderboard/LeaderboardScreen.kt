package com.bansagar.app.ui.leaderboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bansagar.app.R
import com.bansagar.app.data.model.AppUser
import com.bansagar.app.data.model.ContributorStats
import com.bansagar.app.domain.model.ACHIEVEMENTS
import com.bansagar.app.domain.model.Achievement
import com.bansagar.app.domain.model.AchievementTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Leaderboard", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LeaderboardTab(
                label = "Rankings", icon = Icons.Outlined.EmojiEvents,
                selected = selectedTab == 0, onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f),
                primary = primary, onPrimary = onPrimary, onSurfaceVariant = onSurfaceVariant,
            )
            LeaderboardTab(
                label = "Achievements", icon = Icons.Outlined.Stars,
                selected = selectedTab == 1, onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f),
                primary = primary, onPrimary = onPrimary, onSurfaceVariant = onSurfaceVariant,
            )
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = primary)
            }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.error ?: stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::load) { Text(stringResource(R.string.retry)) }
                }
            }
            selectedTab == 0 -> RankingsTab(
                contributors = state.contributors,
                currentUserId = state.currentUser?.id,
                onUserClick = { userId -> viewModel.selectUser(userId); selectedTab = 1 },
            )
            else -> AchievementsTab(
                contributors = state.contributors,
                currentUser = state.currentUser,
                selectedUserId = state.selectedUserId,
                onSelectUser = viewModel::selectUser,
            )
        }
    }
}

@Composable
private fun LeaderboardTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Color,
    onPrimary: Color,
    onSurfaceVariant: Color,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) primary else primary.copy(alpha = 0.1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) onPrimary else onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabContent",
    )

    Box(
        modifier = modifier
            .shadow(if (selected) 6.dp else 0.dp, RoundedCornerShape(50), ambientColor = primary.copy(0.3f), spotColor = primary.copy(0.3f))
            .background(bgColor, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(15.dp), tint = contentColor)
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun RankingsTab(contributors: List<ContributorStats>, currentUserId: String?, onUserClick: (String) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val currentUser = contributors.find { it.authorId == currentUserId }
    val currentUserRank = contributors.indexOfFirst { it.authorId == currentUserId } + 1

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (currentUser != null) {
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = primary.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ContributorAvatar(currentUser, size = 44)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Your Rank", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                            Text("#$currentUserRank of ${contributors.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primary)
                        }
                        StatPill("${currentUser.approvedCount}", "Approved")
                        StatPill("${currentUser.totalUpvotes}", "Votes")
                    }
                }
            }
        }
        if (contributors.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Text("No contributors yet", color = onSurfaceVariant)
                }
            }
        }
        itemsIndexed(contributors) { index, contributor ->
            ContributorRow(
                rank = index + 1, contributor = contributor,
                isCurrentUser = contributor.authorId == currentUserId,
                badgeCount = ACHIEVEMENTS.count { it.check(contributor) },
                onClick = { onUserClick(contributor.authorId) },
            )
        }
    }
}

@Composable
private fun ContributorRow(rank: Int, contributor: ContributorStats, isCurrentUser: Boolean, badgeCount: Int, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = if (isCurrentUser) primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.width(28.dp), contentAlignment = Alignment.Center) {
                when (rank) {
                    1 -> Text("🥇", style = MaterialTheme.typography.titleMedium)
                    2 -> Text("🥈", style = MaterialTheme.typography.titleMedium)
                    3 -> Text("🥉", style = MaterialTheme.typography.titleMedium)
                    else -> Text("#$rank", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            ContributorAvatar(contributor, size = 36)
            Column(modifier = Modifier.weight(1f)) {
                Text(contributor.authorName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                Text("$badgeCount badges", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatPill("${contributor.approvedCount}", "OK")
            StatPill("${contributor.totalUpvotes}", "▲")
        }
    }
}

@Composable
private fun AchievementsTab(contributors: List<ContributorStats>, currentUser: AppUser?, selectedUserId: String?, onSelectUser: (String?) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer

    val syntheticCurrent = currentUser?.let { u ->
        contributors.find { it.authorId == u.id } ?: ContributorStats(
            authorId = u.id, authorName = u.displayName ?: u.email.substringBefore('@').ifEmpty { "You" },
            avatarUrl = u.avatarUrl, approvedCount = 0, totalCount = 0, totalUpvotes = 0, totalViews = 0,
        )
    }
    val viewUser = if (selectedUserId != null) contributors.find { it.authorId == selectedUserId } else syntheticCurrent
    val unlocked = viewUser?.let { u -> ACHIEVEMENTS.filter { it.check(u) } } ?: emptyList()
    val progress = if (ACHIEVEMENTS.isNotEmpty()) unlocked.size.toFloat() / ACHIEVEMENTS.size else 0f

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            if (viewUser != null) {
                Surface(shape = RoundedCornerShape(14.dp), color = surfaceContainer, tonalElevation = 1.dp) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ContributorAvatar(viewUser, size = 48)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(viewUser.authorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text("${unlocked.size}/${ACHIEVEMENTS.size} unlocked", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                            }
                            if (selectedUserId != null && currentUser != null) {
                                TextButton(onClick = { onSelectUser(null) }) { Text("View mine") }
                            }
                        }
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = primary, trackColor = primary.copy(alpha = 0.15f))
                    }
                }
            } else {
                Surface(shape = RoundedCornerShape(14.dp), color = surfaceContainer, tonalElevation = 1.dp) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Sign in to track your achievements", color = onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        items(ACHIEVEMENTS.chunked(2)) { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { achievement ->
                    val isUnlocked = viewUser?.let { achievement.check(it) } ?: false
                    AchievementCard(modifier = Modifier.weight(1f), achievement = achievement, isUnlocked = isUnlocked)
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AchievementCard(modifier: Modifier, achievement: Achievement, isUnlocked: Boolean) {
    val tierColor = when (achievement.tier) {
        AchievementTier.BRONZE -> Color(0xFFCD7F32)
        AchievementTier.SILVER -> Color(0xFFA0A0B0)
        AchievementTier.GOLD -> Color(0xFFFFA000)
        AchievementTier.LEGENDARY -> Color(0xFF9C27B0)
    }
    val alpha = if (isUnlocked) 1f else 0.35f
    val surfaceColor = if (isUnlocked) tierColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceContainer

    Surface(modifier = modifier, color = surfaceColor, shape = RoundedCornerShape(14.dp), tonalElevation = if (isUnlocked) 0.dp else 1.dp) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(tierColor.copy(alpha = if (isUnlocked) 0.18f else 0.06f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(achievement.icon, null, Modifier.size(18.dp), tint = tierColor.copy(alpha = alpha))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(achievement.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(achievement.tier.name.lowercase().replaceFirstChar { it.uppercaseChar() }, style = MaterialTheme.typography.labelSmall, color = tierColor.copy(alpha = alpha))
                }
                if (isUnlocked) Text("✓", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(achievement.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ContributorAvatar(contributor: ContributorStats, size: Int) {
    val primary = MaterialTheme.colorScheme.primary
    if (contributor.avatarUrl != null) {
        AsyncImage(model = contributor.avatarUrl, contentDescription = null, modifier = Modifier.size(size.dp).clip(CircleShape))
    } else {
        Box(
            modifier = Modifier.size(size.dp).clip(CircleShape).background(primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = contributor.authorName.take(1).uppercase(),
                style = if (size >= 44) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
                color = primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StatPill(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
