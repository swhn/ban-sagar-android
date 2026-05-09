package com.bansagar.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansagar.app.R
import com.bansagar.app.domain.model.Timeframe
import com.bansagar.app.ui.components.SlangCard
import com.bansagar.app.ui.theme.Indigo500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSlangClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
        ) {
            Text(
                text = "Ban Sagar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "ဗန်းစကား",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Sort tab row ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SortTab.entries.forEach { tab ->
                SortTabPill(
                    label = when (tab) {
                        SortTab.Trending -> stringResource(R.string.tab_trending)
                        SortTab.Latest   -> stringResource(R.string.tab_latest)
                        SortTab.Top      -> stringResource(R.string.tab_top)
                        SortTab.Random   -> stringResource(R.string.tab_random)
                    },
                    icon = when (tab) {
                        SortTab.Trending -> Icons.Outlined.LocalFireDepartment
                        SortTab.Latest   -> Icons.Outlined.Schedule
                        SortTab.Top      -> Icons.Outlined.EmojiEvents
                        SortTab.Random   -> Icons.Outlined.Shuffle
                    },
                    selected = tab == state.activeTab,
                    onClick = { viewModel.selectTab(tab) },
                )
            }
        }

        // ── Timeframe sub-tab row (Trending only) ────────────────────────
        AnimatedVisibility(
            visible = state.activeTab == SortTab.Trending,
            enter = expandVertically() + fadeIn(tween(180)),
            exit = shrinkVertically() + fadeOut(tween(180)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Timeframe.entries.forEach { tf ->
                    TimeframePill(
                        label = when (tf) {
                            Timeframe.Day   -> stringResource(R.string.tf_day)
                            Timeframe.Week  -> stringResource(R.string.tf_week)
                            Timeframe.Month -> stringResource(R.string.tf_month)
                            Timeframe.Year  -> stringResource(R.string.tf_year)
                        },
                        selected = tf == state.activeTimeframe,
                        onClick = { viewModel.selectTimeframe(tf) },
                    )
                }
            }
        }

        // ── Content ──────────────────────────────────────────────────────
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Indigo500)
            }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error ?: stringResource(R.string.error_generic),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = { viewModel.refresh() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
            else -> {
                val listState = rememberLazyListState()
                val nearBottom by remember {
                    derivedStateOf {
                        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val total = listState.layoutInfo.totalItemsCount
                        total > 0 && last >= total - 4
                    }
                }
                LaunchedEffect(nearBottom) { if (nearBottom) viewModel.loadMore() }

                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(items = state.slangs, key = { it.id }) { slang ->
                            SlangCard(
                                slang = slang,
                                showNsfw = state.showNsfw,
                                onClick = { onSlangClick(slang.slug.ifEmpty { slang.id }) },
                            )
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Indigo500,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable tab components ────────────────────────────────────────────────────

/**
 * Primary sort tab: filled capsule when selected, subtle tinted surface when not.
 * Icon + label side by side.
 */
@Composable
private fun SortTabPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) Indigo500 else Indigo500.copy(alpha = 0.10f),
        animationSpec = tween(durationMillis = 220, easing = EaseInOutCubic),
        label = "tab_bg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Indigo500.copy(alpha = 0.55f),
        animationSpec = tween(durationMillis = 220, easing = EaseInOutCubic),
        label = "tab_content",
    )

    Row(
        modifier = Modifier
            .then(
                if (selected) Modifier.shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = Indigo500.copy(alpha = 0.35f),
                    spotColor = Indigo500.copy(alpha = 0.35f),
                ) else Modifier
            )
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

/**
 * Compact timeframe pill: outlined with indigo accent when selected.
 */
@Composable
private fun TimeframePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) Indigo500.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic),
        label = "tf_bg",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Indigo500 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic),
        label = "tf_text",
    )
    val strokeColor by animateColorAsState(
        targetValue = if (selected) Indigo500 else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic),
        label = "tf_stroke",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            // Draw the border via a thin outline shape overlay
            .then(
                Modifier.shadow(0.dp, RoundedCornerShape(50))
            )
            .background(strokeColor.copy(alpha = 0f))  // placeholder, real border below
            .clickable(onClick = onClick)
            .padding(horizontal = 1.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Inner box provides the visible border by using a slightly smaller fill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(strokeColor)
                .padding(horizontal = 1.dp, vertical = 1.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) Indigo500.copy(alpha = 0.14f) else MaterialTheme.colorScheme.background)
                .padding(horizontal = 13.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}
