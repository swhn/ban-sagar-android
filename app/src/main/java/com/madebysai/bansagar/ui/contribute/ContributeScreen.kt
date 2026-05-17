package com.madebysai.bansagar.ui.contribute

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.madebysai.bansagar.R
import com.madebysai.bansagar.data.model.Slang
import com.madebysai.bansagar.ui.auth.AuthViewModel

private val Amber400 = Color(0xFFFBBF24)
private val Amber500 = Color(0xFFF59E0B)
private val Emerald400 = Color(0xFF34D399)
private val Rose400 = Color(0xFFFB7185)

@Composable
fun ContributeScreen(
    authViewModel: AuthViewModel,
    onNavigateToHistory: () -> Unit,
    onSlangClick: (String) -> Unit = {},
    viewModel: AddSlangViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isSignedIn by authViewModel.isSignedIn.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer

    if (!isSignedIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(primary.copy(alpha = 0.08f), Color.Transparent), endY = 600f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier.size(80.dp).background(primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Edit, null, Modifier.size(36.dp), tint = primary.copy(alpha = 0.7f))
                }
                Text(
                    stringResource(R.string.contribute_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                    color = onSurface,
                )
                Text(
                    stringResource(R.string.contribute_sign_in_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
                Button(
                    onClick = { authViewModel.signIn(context) { _, _ -> } },
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.AccountCircle, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.sign_in_google), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    if (state.submitted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier.size(72.dp).background(primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", style = MaterialTheme.typography.headlineLarge, color = primary)
                }
                Text(
                    stringResource(R.string.contribute_submitted_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                    color = onSurface,
                )
                Text(
                    stringResource(R.string.contribute_submitted_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
                Button(
                    onClick = viewModel::resetForm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.contribute_add_another), fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(onClick = onNavigateToHistory, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.my_submissions))
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.contribute_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = onSurface)
            TextButton(onClick = onNavigateToHistory) {
                Icon(Icons.Outlined.History, null, Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.my_submissions), style = MaterialTheme.typography.labelMedium)
            }
        }

        FormSection(label = "THE WORD", surfaceContainer = surfaceContainer, primary = primary) {
            OutlinedTextField(
                value = state.word,
                onValueChange = viewModel::onWordChange,
                label = { Text(stringResource(R.string.field_word)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    if (state.isCheckingDuplicates)
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = primary)
                },
            )
            OutlinedTextField(
                value = state.pronunciation,
                onValueChange = viewModel::onPronunciationChange,
                label = { Text(stringResource(R.string.field_pronunciation)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
            )

            AnimatedVisibility(
                visible = state.duplicates.isNotEmpty() && !state.dismissedWarning,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                DuplicateWarning(
                    duplicates = state.duplicates,
                    onSlangClick = onSlangClick,
                    onDismiss = viewModel::dismissWarning,
                )
            }
        }

        FormSection(label = "DEFINITIONS", surfaceContainer = surfaceContainer, primary = primary) {
            OutlinedTextField(
                value = state.meaning,
                onValueChange = viewModel::onMeaningChange,
                label = { Text(stringResource(R.string.field_meaning)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(10.dp),
            )
            OutlinedTextField(
                value = state.meaningBurmese,
                onValueChange = viewModel::onMeaningBurmeseChange,
                label = { Text(stringResource(R.string.field_meaning_burmese)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(10.dp),
            )
        }

        FormSection(label = "EXAMPLES", surfaceContainer = surfaceContainer, primary = primary) {
            state.examples.forEachIndexed { index, example ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = example,
                        onValueChange = { viewModel.onExampleChange(index, it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("Example ${index + 1}") },
                    )
                    if (state.examples.size > 1) {
                        IconButton(onClick = { viewModel.removeExample(index) }) {
                            Icon(Icons.Outlined.Close, null, Modifier.size(18.dp), tint = onSurfaceVariant)
                        }
                    }
                }
            }
            if (state.examples.size < 5) {
                TextButton(onClick = viewModel::addExample) {
                    Icon(Icons.Outlined.Add, null, Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.add_example), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Surface(shape = RoundedCornerShape(14.dp), color = surfaceContainer, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.field_nsfw), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = onSurface)
                    Text("Mark as sensitive / adult content", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                }
                Switch(checked = state.isNsfw, onCheckedChange = viewModel::onNsfwChange)
            }
        }

        state.error?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = viewModel::submit,
            enabled = !state.isSubmitting
                && state.word.isNotBlank()
                && state.pronunciation.isNotBlank()
                && state.meaningBurmese.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primary),
            shape = RoundedCornerShape(12.dp),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.submit), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun DuplicateWarning(
    duplicates: List<Slang>,
    onSlangClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Amber500.copy(alpha = 0.10f))
            .border(1.dp, Amber500.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Outlined.Search, null, Modifier.size(16.dp), tint = Amber400)
            Text(
                stringResource(R.string.duplicate_warning),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Amber400,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            duplicates.forEach { dup ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .clickable { onSlangClick(dup.slug.ifEmpty { dup.id }) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        dup.word,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusBadge(dup.status)
                    Text(
                        dup.meaningBurmese?.takeIf { it.isNotBlank() } ?: dup.meaning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                stringResource(R.string.not_duplicate),
                style = MaterialTheme.typography.labelSmall,
                color = Amber400.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "approved" -> Emerald400.copy(alpha = 0.15f) to Emerald400
        "pending"  -> Amber400.copy(alpha = 0.15f) to Amber400
        else       -> Rose400.copy(alpha = 0.15f) to Rose400
    }
    Text(
        status.uppercase(),
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = fg,
    )
}

@Composable
private fun FormSection(
    label: String,
    surfaceContainer: androidx.compose.ui.graphics.Color,
    primary: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit,
) {
    Surface(shape = RoundedCornerShape(14.dp), color = surfaceContainer, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = primary.copy(alpha = 0.75f), fontWeight = FontWeight.Bold)
            content()
        }
    }
}
