package com.bansagar.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
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

    // Activity-scoped: shared down to all screens that need it
    val authViewModel: AuthViewModel = hiltViewModel()

    // Tracks auth session restore + Room warmup; shows loading overlay until done
    val initViewModel: AppInitViewModel = hiltViewModel()
    val isInitializing by initViewModel.isInitializing.collectAsStateWithLifecycle()

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

        // Branded loading overlay — sits on top of everything while the app initialises.
        // AnimatedVisibility with fadeOut gives a smooth handoff to the home screen content.
        AnimatedVisibility(
            visible = isInitializing,
            enter = EnterTransition.None,   // already visible at startup; no enter animation needed
            exit = fadeOut(animationSpec = tween(durationMillis = 450)),
            modifier = Modifier.fillMaxSize(),
        ) {
            SplashLoadingScreen()
        }
    }
}
