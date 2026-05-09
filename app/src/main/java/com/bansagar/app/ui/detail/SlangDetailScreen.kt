package com.bansagar.app.ui.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansagar.app.R

private val Indigo500 = Color(0xFF6366F1)
private val Rose400 = Color(0xFFFB7185)
private val DetailSurface = Color(0xFF16161E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SlangDetailScreen(
    slug: String,
    onBack: () -> Unit,
    onSlangClick: (String) -> Unit,
    viewModel: SlangDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val slang = state.slang
    val context = LocalContext.current
    val isBlurred = slang != null && slang.isNsfw && !state.showNsfw

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                val title = when {
                    slang == null -> ""
                    isBlurred -> "••••••"
                    else -> slang.word
                }
                Text(text = title, fontWeight = FontWeight.SemiBold)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            actions = {
                if (slang != null && !isBlurred) {
                    IconButton(onClick = {
                        val shareUrl = "https://bansagar.com/slang/${slang.slug.ifEmpty { slang.id }}"
                        val shareText = "${slang.word} — ${slang.meaning}\n$shareUrl"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.share))
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Indigo500)
            }
            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(state.error ?: stringResource(R.string.error_generic),
                    color = MaterialTheme.colorScheme.error)
            }
            slang != null && isBlurred -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.25f),
                        )
                        Text(
                            text = stringResource(R.string.nsfw_gate_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(R.string.nsfw_gate_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            slang != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Hero card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Indigo500.copy(alpha = 0.08f),
                                RoundedCornerShape(20.dp),
                            )
                            .padding(20.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = slang.word,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Indigo500,
                            )
                            if (!slang.pronunciation.isNullOrBlank()) {
                                Text(
                                    text = "/${slang.pronunciation}/",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontStyle = FontStyle.Italic,
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                VotePill(
                                    active = state.userVote == "up",
                                    activeColor = Indigo500,
                                    enabled = !state.isVoting,
                                    onClick = { viewModel.castVote("up") },
                                    icon = {
                                        Icon(
                                            imageVector = if (state.userVote == "up") Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                            contentDescription = stringResource(R.string.upvote),
                                            modifier = Modifier.size(15.dp),
                                            tint = if (state.userVote == "up") Color.White else Indigo500,
                                        )
                                    },
                                    label = "${slang.upvotes}",
                                    labelColor = if (state.userVote == "up") Color.White else Indigo500,
                                )
                                VotePill(
                                    active = state.userVote == "down",
                                    activeColor = Rose400,
                                    enabled = !state.isVoting,
                                    onClick = { viewModel.castVote("down") },
                                    icon = {
                                        Icon(
                                            imageVector = if (state.userVote == "down") Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                            contentDescription = stringResource(R.string.downvote),
                                            modifier = Modifier.size(15.dp),
                                            tint = if (state.userVote == "down") Color.White else Rose400,
                                        )
                                    },
                                    label = "${slang.downvotes}",
                                    labelColor = if (state.userVote == "down") Color.White else Rose400,
                                )
                                Spacer(Modifier.width(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Outlined.Visibility, null,
                                        Modifier.size(14.dp),
                                        tint = Color.White.copy(alpha = 0.3f),
                                    )
                                    Text(
                                        "${slang.views}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.35f),
                                    )
                                }
                            }
                        }
                    }

                    if (slang.meaning.isNotBlank()) {
                        SectionCard(title = stringResource(R.string.meaning)) {
                            Text(
                                slang.meaning,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                    }
                    if (!slang.meaningBurmese.isNullOrBlank()) {
                        SectionCard(title = stringResource(R.string.meaning_burmese)) {
                            Text(
                                slang.meaningBurmese,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                    }
                    if (slang.examples.isNotEmpty()) {
                        SectionCard(title = stringResource(R.string.examples)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                slang.examples.forEachIndexed { i, example ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Text(
                                            "${i + 1}.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Indigo500.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            "\"$example\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontStyle = FontStyle.Italic,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (state.relatedWords.isNotEmpty()) {
                        Column {
                            Text(
                                text = stringResource(R.string.related_words),
                                style = MaterialTheme.typography.labelLarge,
                                color = Indigo500.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                state.relatedWords.forEach { related ->
                                    Surface(
                                        onClick = { onSlangClick(related.slug.ifEmpty { related.id }) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = DetailSurface,
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                            Text(
                                                related.word,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Indigo500.copy(alpha = 0.85f),
                                            )
                                            Text(
                                                related.meaning,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = Color.White.copy(alpha = 0.4f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun VotePill(
    active: Boolean,
    activeColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    labelColor: Color,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        color = if (active) activeColor else activeColor.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = labelColor,
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DetailSurface),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = Indigo500.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            content()
        }
    }
}
