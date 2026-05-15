package com.bansagar.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.bansagar.app.R
import com.bansagar.app.ui.auth.AuthViewModel
import com.bansagar.app.ui.contribute.ContributeScreen
import com.bansagar.app.ui.detail.SlangDetailScreen
import com.bansagar.app.ui.history.HistoryScreen
import com.bansagar.app.ui.home.HomeScreen
import com.bansagar.app.ui.init.AppInitViewModel
import com.bansagar.app.ui.leaderboard.LeaderboardScreen
import com.bansagar.app.ui.profile.ProfileScreen
import com.bansagar.app.ui.search.SearchScreen
import com.bansagar.app.ui.splash.SplashLoadingScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val CONTRIBUTE = "contribute"
    const val PROFILE = "profile"
    const val HISTORY = "history"
    const val LEADERBOARD = "leaderboard"
    const val SLANG_DETAIL = "slang/{slug}"

    fun slangDetail(slug: String) = "slang/$slug"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authViewModel: AuthViewModel = hiltViewModel()
    val initViewModel: AppInitViewModel = hiltViewModel()
    val isInitializing by initViewModel.isInitializing.collectAsStateWithLifecycle()
    val siteSettings by initViewModel.siteSettings.collectAsStateWithLifecycle()

    // Show announcement once per session after init completes
    var announcementDismissed by remember { mutableStateOf(false) }
    val announcement = siteSettings.siteAnnouncement?.takeIf { it.isNotBlank() }

    if (!isInitializing && announcement != null && !announcementDismissed) {
        AlertDialog(
            onDismissRequest = { announcementDismissed = true },
            title = {
                Text(
                    stringResource(R.string.announcement_title),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = { Text(announcement) },
            confirmButton = {
                TextButton(onClick = { announcementDismissed = true }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    val showBottomBar = currentRoute in listOf(
        Routes.HOME, Routes.SEARCH, Routes.CONTRIBUTE, Routes.PROFILE
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onSlangClick = { slug -> navController.navigate(Routes.slangDetail(slug)) },
                    )
                }
                composable(Routes.SEARCH) {
                    SearchScreen(
                        onSlangClick = { slug -> navController.navigate(Routes.slangDetail(slug)) },
                    )
                }
                composable(Routes.CONTRIBUTE) {
                    ContributeScreen(
                        authViewModel = authViewModel,
                        onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        showRanking = siteSettings.showRanking,
                        onNavigateToLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                    )
                }
                composable(Routes.HISTORY) {
                    HistoryScreen(
                        onBack = { navController.popBackStack() },
                        onSlangClick = { slug -> navController.navigate(Routes.slangDetail(slug)) },
                    )
                }
                composable(Routes.LEADERBOARD) {
                    LeaderboardScreen(
                        onBack = { navController.popBackStack() },
                        showRanking = siteSettings.showRanking,
                    )
                }
                composable(
                    route = Routes.SLANG_DETAIL,
                    arguments = listOf(navArgument("slug") { type = NavType.StringType }),
                    deepLinks = listOf(navDeepLink { uriPattern = "https://bansagar.com/slang/{slug}" }),
                ) { backStackEntry ->
                    val slug = backStackEntry.arguments?.getString("slug") ?: return@composable
                    SlangDetailScreen(
                        slug = slug,
                        onBack = { navController.popBackStack() },
                        onSlangClick = { s -> navController.navigate(Routes.slangDetail(s)) },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isInitializing,
            enter = EnterTransition.None,
            exit = fadeOut(animationSpec = tween(durationMillis = 450)),
            modifier = Modifier.fillMaxSize(),
        ) {
            SplashLoadingScreen()
        }
    }
}
