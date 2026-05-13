package com.bansagar.app.ui.profile

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
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bansagar.app.R
import com.bansagar.app.ui.auth.AuthViewModel
import kotlinx.coroutines.delay

private val Emerald400 = Color(0xFF34D399)
private val Amber400 = Color(0xFFFBBF24)

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateToLeaderboard: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val showNsfw by viewModel.showNsfw.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer

    if (!state.isLoading && state.user == null) {
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
                    Icon(Icons.Outlined.Person, null, Modifier.size(40.dp), tint = primary.copy(alpha = 0.7f))
                }
                Text(
                    stringResource(R.string.sign_in_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                    color = onSurface,
                )
                Text(
                    stringResource(R.string.sign_in_subtitle),
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

    val user = state.user ?: return

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(primary.copy(alpha = 0.15f), Color.Transparent)))
                .padding(top = 32.dp, bottom = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (user.avatarUrl != null) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).border(2.dp, primary.copy(alpha = 0.6f), CircleShape).clip(CircleShape),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(2.dp, primary.copy(alpha = 0.6f), CircleShape)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (user.displayName ?: user.email).take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Text(
                    user.displayName ?: user.email.substringBefore('@'),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                )
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                if (user.role != "user") {
                    Surface(shape = RoundedCornerShape(50), color = primary.copy(alpha = 0.12f)) {
                        Text(
                            text = user.role.replaceFirstChar { it.uppercaseChar() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Edit,
                    label = stringResource(R.string.stat_submitted),
                    value = "${state.stats.submittedCount}",
                    tint = primary,
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Star,
                    label = stringResource(R.string.stat_approved),
                    value = "${state.stats.approvedCount}",
                    tint = Emerald400,
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.ThumbUp,
                    label = stringResource(R.string.stat_upvotes),
                    value = "${state.stats.totalUpvotes}",
                    tint = Amber400,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToLeaderboard() },
                shape = RoundedCornerShape(14.dp),
                color = surfaceContainer,
                tonalElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).background(Amber400.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.EmojiEvents, null, Modifier.size(20.dp), tint = Amber400)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.leaderboard), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = onSurface)
                        Text("Rankings & achievements", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = onSurfaceVariant)
                }
            }

            Surface(shape = RoundedCornerShape(14.dp), color = surfaceContainer, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(stringResource(R.string.profile_display_name), style = MaterialTheme.typography.labelMedium, color = primary, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = state.displayNameInput,
                        onValueChange = viewModel::onDisplayNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    Button(
                        onClick = viewModel::saveDisplayName,
                        enabled = !state.isSaving && state.displayNameInput.trim().isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(stringResource(R.string.save), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(14.dp), color = surfaceContainer, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        stringResource(R.string.profile_preferences),
                        style = MaterialTheme.typography.labelMedium,
                        color = primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                    )
                    PrefRow(stringResource(R.string.show_sensitive), showNsfw, viewModel::setShowNsfw)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    PrefRow(stringResource(R.string.pref_notify_approved), user.notifyApproved, viewModel::toggleNotifyApproved)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    PrefRow(stringResource(R.string.pref_notify_badges), user.notifyBadges, viewModel::toggleNotifyBadges)
                }
            }

            OutlinedButton(
                onClick = authViewModel::signOut,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Outlined.ExitToApp, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.sign_out))
            }

            state.successMessage?.let { msg ->
                LaunchedEffect(msg) { delay(2000); viewModel.dismissMessage() }
                Text(msg, color = primary, style = MaterialTheme.typography.labelMedium)
            }
            state.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, icon: ImageVector, label: String, value: String, tint: Color) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 1.dp) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(tint.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(16.dp), tint = tint)
            }
            Text(value, style = MaterialTheme.typography.titleLarge, color = tint, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PrefRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
