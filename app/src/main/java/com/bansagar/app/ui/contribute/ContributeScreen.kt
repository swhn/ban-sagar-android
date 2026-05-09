package com.bansagar.app.ui.contribute

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansagar.app.R
import com.bansagar.app.ui.auth.AuthViewModel

private val Indigo500 = Color(0xFF6366F1)
private val Indigo400 = Color(0xFF818CF8)
private val SurfaceCard = Color(0xFF16161E)
private val Rose400 = Color(0xFFFB7185)
private val Amber400 = Color(0xFFFBBF24)

@Composable
fun ContributeScreen(
    authViewModel: AuthViewModel,
    onNavigateToHistory: () -> Unit,
    viewModel: AddSlangViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isSignedIn by authViewModel.isSignedIn.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (!isSignedIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Indigo500.copy(alpha = 0.08f), Color.Transparent),
                        endY = 600f,
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Indigo500.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Edit, null, Modifier.size(36.dp), tint = Indigo500.copy(alpha = 0.6f))
                }
                Text(
                    stringResource(R.string.contribute_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    stringResource(R.string.contribute_sign_in_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
                Button(
                    onClick = { authViewModel.signIn(context) { _, _ -> } },
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Indigo500.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", style = MaterialTheme.typography.headlineLarge, color = Indigo400)
                }
                Text(
                    stringResource(R.string.contribute_submitted_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    stringResource(R.string.contribute_submitted_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
                Button(
                    onClick = viewModel::resetForm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.contribute_add_another), fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.my_submissions))
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.contribute_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onNavigateToHistory) {
                Icon(Icons.Outlined.History, null, Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.my_submissions), style = MaterialTheme.typography.labelMedium)
            }
        }

        // Section: The Word
        FormSection(label = "THE WORD") {
            OutlinedTextField(
                value = state.word,
                onValueChange = viewModel::onWordChange,
                label = { Text(stringResource(R.string.field_word)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    if (state.isCheckingDuplicates)
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Indigo400)
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
            if (state.duplicates.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Rose400.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(Icons.Outlined.WarningAmber, null, Modifier.size(16.dp), tint = Rose400)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            stringResource(R.string.duplicate_warning),
                            style = MaterialTheme.typography.labelSmall,
                            color = Rose400,
                            fontWeight = FontWeight.SemiBold,
                        )
                        state.duplicates.forEach { dup ->
                            Text(
                                "• ${dup.word} (${dup.status})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.55f),
                            )
                        }
                    }
                }
            }
        }

        // Section: Definitions
        FormSection(label = "DEFINITIONS") {
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

        // Section: Examples
        FormSection(label = "EXAMPLES") {
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
                            Icon(Icons.Outlined.Close, null, Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.4f))
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

        // Section: Options
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceCard,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.field_nsfw),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        "Mark as sensitive / adult content",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                    )
                }
                Switch(
                    checked = state.isNsfw,
                    onCheckedChange = viewModel::onNsfwChange,
                )
            }
        }

        state.error?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = viewModel::submit,
            enabled = !state.isSubmitting && state.word.isNotBlank() && state.meaning.isNotBlank() && state.meaningBurmese.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
            shape = RoundedCornerShape(12.dp),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(stringResource(R.string.submit), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun FormSection(
    label: String,
    content: @Composable Column.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Indigo400.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
            )
            content()
        }
    }
}
