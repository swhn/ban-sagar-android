package com.bansagar.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.WbSunny
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansagar.app.R
import com.bansagar.app.data.model.Slang
import com.bansagar.app.domain.model.Timeframe
import com.bansagar.app.ui.components.SlangCard
import com.bansagar.app.ui.theme.Indigo500
import kotlinx.coroutines.launch

private val Amber400 = Color(0xFFFBBF24)
private val Amber500 = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onSlangClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = SortTab.entries
    val pagerState = rememberPagerState(
        initialPage = tabs.indexOf(state.activeTab).coerceAtLeast(0),
        pageCount = { tabs.size },
    )
    val scope = rememberCoroutineScope()

    // User swiped to a new page → update ViewModel
    LaunchedEffect(pagerState.settledPage) {
        val newTab = tabs[pagerState.settledPage]
        if (newTab != state.activeTab) viewModel.selectTab(newTab)
    }

    // Tab pill tapped (ViewModel changed) → scroll pager to match
    LaunchedEffect(state.activeTab) {
        val idx = tabs.indexOf(state.activeTab)
        if (idx >= 0 && pagerState.currentPage != idx && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(idx)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ───────────────────────────────────────────────────────────────────────
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

        // ── Word of the Day card ──────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.wordOfTheDay != null,
            enter = expandVertically() + fadeIn(tween(300)),
            exit = shrinkVertically() + fadeOut(tween(200)),
        ) {
            state.wordOfTheDay?.let { wotd ->
                WordOfTheDayCard(
                    slang = wotd,
                    onClick = { onSlangClick(wotd.slug.ifEmpty { wotd.id }) },
                )
            }
        }

        // ── Sort tab row ─────────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tabs.forEach { tab ->
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
                    selected = tab == tabs[pagerState.currentPage],
                    onClick = { scope.launch { pagerState.animateScrollToPage(tabs.indexOf(tab)) } },
                )
            }
        }

        // ── Timeframe sub-tab row (Trending only) ────────────────────────────────────────
        AnimatedVisibility(
            visible = tabs.getOrElse(pagerState.currentPage) { state.activeTab } == SortTab.Trending,
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

        // ── Swipeable content pager ───────────────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 0,
        ) { page ->
            if (tabs[page] == state.activeTab) {
                TabContent(
                    state = state,
                    onSlangClick = onSlangClick,
                    onRetry = { viewModel.refresh() },
                    onLoadMore = { viewModel.loadMore() },
                )
            } else {
                Box(Modifier.fillMaxSize())
            }
        }
    }
}

// ── Tab content ──────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabContent(
    state: HomeUiState,
    onSlangClick: (String) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
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
                TextButton(onClick = onRetry) {
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
            LaunchedEffect(nearBottom) { if (nearBottom) onLoadMore() }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRetry,
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

// ── Word of the Day card ─────────────────────────────────────────────────────────────────────────

@Composable
private fun WordOfTheDayCard(slang: Slang, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Amber500.copy(alpha = 0.13f))
            .border(1.dp, Amber500.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Amber500.copy(alpha = 0.20f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.WbSunny, null, Modifier.size(22.dp), tint = Amber400)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(R.string.wotd_label),
                style = MaterialTheme.typography.labelSmall,
                color = Amber500,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
            )
            Text(
                slang.word,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                slang.meaningBurmese?.takeIf { it.isNotBlank() } ?: slang.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, Modifier.size(18.dp), tint = Amber400)
    }
}

// ── Tab components ───────────────────────────────────────────────────────────────────────────────

@Composable
private fun SortTabPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) Indigo500 else Indigo500.copy(alpha = 0.10f),
        animationSpec = tween(220, easing = EaseInOutCubic),
        label = "sort_tab_bg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Indigo500.copy(alpha = 0.55f),
        animationSpec = tween(220, easing = EaseInOutCubic),
        label = "sort_tab_content",
    )

    Row(
        modifier = Modifier
            .then(
                if (selected) Modifier.shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = Indigo500.copy(alpha = 0.40f),
                    spotColor = Indigo500.copy(alpha = 0.40f),
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

@Composable
private fun TimeframePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) Indigo500.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = tween(200, easing = EaseInOutCubic),
        label = "tf_bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Indigo500 else Indigo500.copy(alpha = 0.22f),
        animationSpec = tween(200, easing = EaseInOutCubic),
        label = "tf_border",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Indigo500 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        animationSpec = tween(200, easing = EaseInOutCubic),
        label = "tf_text",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
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
