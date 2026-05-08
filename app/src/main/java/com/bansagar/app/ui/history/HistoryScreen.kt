package com.bansagar.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansagar.app.R
import com.bansagar.app.ui.components.SlangCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onSlangClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val displayed = if (state.filterStatus == null) state.slangs
    else state.slangs.filter { it.status == state.filterStatus }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.my_submissions)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(null, "pending", "approved", "rejected").forEach { status ->
                FilterChip(
                    selected = state.filterStatus == status,
                    onClick = { viewModel.setFilter(status) },
                    label = {
                        Text(
                            when (status) {
                                null -> stringResource(R.string.filter_all)
                                "pending" -> stringResource(R.string.status_pending)
                                "approved" -> stringResource(R.string.status_approved)
                                else -> stringResource(R.string.status_rejected)
                            }
                        )
                    },
                )
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            displayed.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    text = stringResource(R.string.empty_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = displayed, key = { it.id }) { slang ->
                    Column {
                        SlangCard(
                            slang = slang,
                            showNsfw = true,
                            onClick = { onSlangClick(slang.slug.ifEmpty { slang.id }) },
                        )
                        StatusBadge(slang.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, label) = when (status) {
        "approved" -> MaterialTheme.colorScheme.primary to stringResource(R.string.status_approved)
        "rejected" -> MaterialTheme.colorScheme.error to stringResource(R.string.status_rejected)
        else -> MaterialTheme.colorScheme.outline to stringResource(R.string.status_pending)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
    )
}
