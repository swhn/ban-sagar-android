package com.bansagar.app.ui.history

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansagar.app.R
import com.bansagar.app.ui.components.SlangCard

private val Emerald400 = Color(0xFF34D399)
private val Amber400 = Color(0xFFFBBF24)
private val Rose400 = Color(0xFFFB7185)

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

    val labelAll = stringResource(R.string.filter_all)
    val labelPending = stringResource(R.string.status_pending)
    val labelApproved = stringResource(R.string.status_approved)
    val labelRejected = stringResource(R.string.status_rejected)
    val filters = listOf(
        null to labelAll,
        "pending" to labelPending,
        "approved" to labelApproved,
        "rejected" to labelRejected,
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.my_submissions), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filters) { (status, label) ->
                StatusFilterPill(label = label, status = status, selected = state.filterStatus == status, onClick = { viewModel.setFilter(status) })
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            displayed.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(stringResource(R.string.empty_history), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items = displayed, key = { it.id }) { slang ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SlangCard(slang = slang, showNsfw = true, onClick = { onSlangClick(slang.slug.ifEmpty { slang.id }) })
                        StatusBadge(slang.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilterPill(label: String, status: String?, selected: Boolean, onClick: () -> Unit) {
    val accentColor = when (status) {
        "approved" -> Emerald400
        "rejected" -> Rose400
        "pending" -> Amber400
        else -> MaterialTheme.colorScheme.primary
    }
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val bgColor by animateColorAsState(targetValue = if (selected) accentColor else accentColor.copy(alpha = 0.12f), animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "filterBg")
    val textColor by animateColorAsState(
        targetValue = if (selected) { if (status == null) onPrimary else Color.White } else accentColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "filterText",
    )
    Box(
        modifier = Modifier
            .shadow(if (selected) 6.dp else 0.dp, RoundedCornerShape(50), ambientColor = accentColor.copy(0.25f), spotColor = accentColor.copy(0.25f))
            .background(bgColor, RoundedCornerShape(50)).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = textColor, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, label) = when (status) {
        "approved" -> Emerald400 to stringResource(R.string.status_approved)
        "rejected" -> Rose400 to stringResource(R.string.status_rejected)
        else -> Amber400 to stringResource(R.string.status_pending)
    }
    Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp))
}
