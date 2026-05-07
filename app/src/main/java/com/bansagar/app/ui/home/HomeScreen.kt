package com.bansagar.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansagar.app.R
import com.bansagar.app.domain.model.Timeframe
import com.bansagar.app.ui.components.SlangCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSlangClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Ban Sagar",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "ဗန်းစကား",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ScrollableTabRow(
            selectedTabIndex = SortTab.entries.indexOf(state.activeTab),
            edgePadding = 16.dp,
            divider = {},
            indicator = {},
        ) {
            SortTab.entries.forEach { tab ->
                val selected = tab == state.activeTab
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.selectTab(tab) },
                    label = {
                        Text(
                            text = when (tab) {
                                SortTab.Trending -> stringResource(R.string.tab_trending)
                                SortTab.Latest -> stringResource(R.string.tab_latest)
                                SortTab.Top -> stringResource(R.string.tab_top)
                                SortTab.Random -> stringResource(R.string.tab_random)
                            },
                        )
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        AnimatedVisibility(visible = state.activeTab == SortTab.Trending) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Timeframe.entries.forEach { tf ->
                    val selected = tf == state.activeTimeframe
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectTimeframe(tf) },
                        label = {
                            Text(
                                text = when (tf) {
                                    Timeframe.Day -> stringResource(R.string.tf_day)
                                    Timeframe.Week -> stringResource(R.string.tf_week)
                                    Timeframe.Month -> stringResource(R.string.tf_month)
                                    Timeframe.Year -> stringResource(R.string.tf_year)
                                },
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                    )
                }
            }
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
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
                LaunchedEffect(nearBottom) {
                    if (nearBottom) viewModel.loadMore()
                }

                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = state.slangs,
                            key = { it.id },
                        ) { slang ->
                            SlangCard(
                                slang = slang,
                                onClick = { onSlangClick(slang.slug.ifEmpty { slang.id }) },
                            )
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
