package com.madebysai.bansagar.ui.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.madebysai.bansagar.R
import com.madebysai.bansagar.data.preferences.ThemeMode
import com.madebysai.bansagar.ui.auth.AuthViewModel
import kotlinx.coroutines.delay

private val Emerald400 = Color(0xFF34D399)
private val Amber400   = Color(0xFFFBBF24)

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    showRanking: Boolean = true,
    onNavigateToLeaderboard: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state             by viewModel.uiState.collectAsStateWithLifecycle()
    val showNsfw          by viewModel.showNsfw.collectAsStateWithLifecycle()
    val themeMode         by viewModel.themeMode.collectAsStateWithLifecycle()
    val wotdNotifications by viewModel.wotdNotifications.collectAsStateWithLifecycle()
    val context           = LocalContext.current
    val uriHandler        = LocalUriHandler.current
    val primary           = MaterialTheme.colorScheme.primary
    val onPrimary         = MaterialTheme.colorScheme.onPrimary
    val onSurface         = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant  = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainer  = MaterialTheme.colorScheme.surfaceContainer

    var showNsfwDialog by remember { mutableStateOf(false) }

    if (showNsfwDialog) {
        AlertDialog(
            onDismissRequest = { showNsfwDialog = false },
            icon = { Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.nsfw_warning_title), fontWeight = FontWeight.Bold) },
            text  = { Text(stringResource(R.string.nsfw_warning_body)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.setShowNsfw(true); showNsfwDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.nsfw_warning_confirm), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showNsfwDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    // ── Signed-out ──────────────────────────────────────────────────────────────────────────
    if (!state.isLoading && state.user == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Sign-in card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = surfaceContainer,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).background(primary.copy(alpha = 0.10f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Person, null, Modifier.size(36.dp), tint = primary.copy(alpha = 0.7f))
                    }
                    Text(
                        stringResource(R.string.sign_in_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                        color = onSurface,
                    )
                    Text(
                        stringResource(R.string.sign_in_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp),
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

            // Leaderboard / Achievements row
            LeaderboardRow(
                showRanking = showRanking,
                surfaceContainer = surfaceContainer,
                onSurface = onSurface,
                onSurfaceVariant = onSurfaceVariant,
                onClick = onNavigateToLeaderboard,
            )

            // Appearance
            SettingsSection(stringResource(R.string.pref_section_appearance), surfaceContainer, primary) {
                ThemePills(themeMode, primary, onPrimary, onSurfaceVariant) { viewModel.setThemeMode(it) }
            }

            // Notifications
            SettingsSection(stringResource(R.string.pref_section_notifications), surfaceContainer, primary) {
                PrefRow(
                    label = stringResource(R.string.pref_wotd_notifications),
                    description = stringResource(R.string.wotd_channel_desc),
                    checked = wotdNotifications,
                    onToggle = { viewModel.setWotdNotifications(it) },
                )
            }

            LinkSection(uriHandler, surfaceContainer, onSurface, onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
        }
        return
    }

    // ── Signed-in ────────────────────────────────────────────────────────────────────────────
    val user = state.user ?: return

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

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
                            (user.displayName ?: user.email).take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Text(user.displayName ?: user.email.substringBefore('@'), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = onSurface)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                if (user.role != "user") {
                    Surface(shape = RoundedCornerShape(50), color = primary.copy(alpha = 0.12f)) {
                        Text(
                            user.role.replaceFirstChar { it.uppercaseChar() },
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

            // Stats
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(Modifier.weight(1f), Icons.Outlined.Edit,    stringResource(R.string.stat_submitted), "${state.stats.submittedCount}", primary)
                StatCard(Modifier.weight(1f), Icons.Outlined.Star,    stringResource(R.string.stat_approved),  "${state.stats.approvedCount}",  Emerald400)
                StatCard(Modifier.weight(1f), Icons.Outlined.ThumbUp, stringResource(R.string.stat_upvotes),   "${state.stats.totalUpvotes}",    Amber400)
            }

            // Leaderboard / Achievements row
            LeaderboardRow(
                showRanking = showRanking,
                surfaceContainer = surfaceContainer,
                onSurface = onSurface,
                onSurfaceVariant = onSurfaceVariant,
                onClick = onNavigateToLeaderboard,
            )

            // Display name
            SettingsSection(stringResource(R.string.profile_display_name).uppercase(), surfaceContainer, primary) {
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

            // Appearance
            SettingsSection(stringResource(R.string.pref_section_appearance), surfaceContainer, primary) {
                ThemePills(themeMode, primary, onPrimary, onSurfaceVariant) { viewModel.setThemeMode(it) }
            }

            // Notifications
            SettingsSection(stringResource(R.string.pref_section_notifications), surfaceContainer, primary) {
                PrefRow(
                    label = stringResource(R.string.pref_wotd_notifications),
                    description = stringResource(R.string.wotd_channel_desc),
                    checked = wotdNotifications,
                    onToggle = { viewModel.setWotdNotifications(it) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                PrefRow(stringResource(R.string.pref_notify_approved), null, user.notifyApproved, viewModel::toggleNotifyApproved)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                PrefRow(stringResource(R.string.pref_notify_badges), null, user.notifyBadges, viewModel::toggleNotifyBadges)
            }

            // Content
            SettingsSection(stringResource(R.string.pref_section_content), surfaceContainer, primary) {
                PrefRow(
                    label = stringResource(R.string.show_sensitive),
                    description = stringResource(R.string.show_sensitive_desc),
                    checked = showNsfw,
                    onToggle = { if (it) showNsfwDialog = true else viewModel.setShowNsfw(false) },
                )
            }

            LinkSection(uriHandler, surfaceContainer, onSurface, onSurfaceVariant)

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

// ── Shared sub-components ───────────────────────────────────────────────────────────────────────

@Composable
private fun LeaderboardRow(
    showRanking: Boolean,
    surfaceContainer: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                Icon(
                    if (showRanking) Icons.Outlined.EmojiEvents else Icons.Outlined.Stars,
                    null, Modifier.size(20.dp), tint = Amber400,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (showRanking) stringResource(R.string.leaderboard) else stringResource(R.string.leaderboard_achievements),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurface,
                )
                Text(
                    if (showRanking) "Rankings & achievements" else "Badges & achievements",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    surfaceContainer: Color,
    primary: Color,
    content: @Composable () -> Unit,
) {
    Surface(shape = RoundedCornerShape(14.dp), color = surfaceContainer, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = primary.copy(alpha = 0.75f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
            )
            content()
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ThemePills(
    current: ThemeMode,
    primary: Color,
    onPrimary: Color,
    onSurfaceVariant: Color,
    onSelect: (ThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ThemeMode.entries.forEach { mode ->
            val selected = current == mode
            val bg by animateColorAsState(
                targetValue = if (selected) primary else primary.copy(alpha = 0.10f),
                animationSpec = tween(200),
                label = "themePillBg",
            )
            val fg by animateColorAsState(
                targetValue = if (selected) onPrimary else onSurfaceVariant,
                animationSpec = tween(200),
                label = "themePillFg",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = when (mode) {
                            ThemeMode.SYSTEM -> Icons.Outlined.Brightness4
                            ThemeMode.LIGHT  -> Icons.Outlined.WbSunny
                            ThemeMode.DARK   -> Icons.Outlined.NightsStay
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = fg,
                    )
                    Text(
                        text = when (mode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                            ThemeMode.LIGHT  -> stringResource(R.string.theme_light)
                            ThemeMode.DARK   -> stringResource(R.string.theme_dark)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = fg,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun PrefRow(
    label: String,
    description: String?,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun LinkSection(uriHandler: UriHandler, surfaceContainer: Color, onSurface: Color, onSurfaceVariant: Color) {
    val links = listOf(
        Triple(Icons.Outlined.Info,       R.string.link_about,   "https://bansagar.com/about"),
        Triple(Icons.Outlined.Mail,       R.string.link_contact, "https://bansagar.com/contact"),
        Triple(Icons.Outlined.PrivacyTip, R.string.link_privacy, "https://bansagar.com/privacy"),
    )
    Surface(shape = RoundedCornerShape(14.dp), color = surfaceContainer, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            links.forEachIndexed { index, (icon, labelRes, url) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(url) }.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(icon, null, Modifier.size(20.dp), tint = onSurfaceVariant)
                    Text(stringResource(labelRes), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = onSurface)
                    Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, Modifier.size(18.dp), tint = onSurfaceVariant)
                }
                if (index < links.lastIndex) HorizontalDivider(modifier = Modifier.padding(start = 44.dp), color = MaterialTheme.colorScheme.outlineVariant)
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
            Box(modifier = Modifier.size(32.dp).background(tint.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(16.dp), tint = tint)
            }
            Text(value, style = MaterialTheme.typography.titleLarge, color = tint, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
