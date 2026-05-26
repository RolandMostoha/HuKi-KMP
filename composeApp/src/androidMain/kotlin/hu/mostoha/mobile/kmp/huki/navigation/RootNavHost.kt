package hu.mostoha.mobile.kmp.huki.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.mostoha.mobile.kmp.huki.ui.features.main.MainScreen
import hu.mostoha.mobile.kmp.huki.ui.features.settings.SettingsScreen
import hu.mostoha.mobile.kmp.huki.util.AnimationConstants.NAVIGATION_TRANSITION_DURATION
import hu.mostoha.mobile.kmp.huki.util.millis

private object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
}

@Composable
fun RootNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(NAVIGATION_TRANSITION_DURATION.millis()),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(NAVIGATION_TRANSITION_DURATION.millis()),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(NAVIGATION_TRANSITION_DURATION.millis()),
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(NAVIGATION_TRANSITION_DURATION.millis()),
            )
        },
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                onSettingsClicked = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
