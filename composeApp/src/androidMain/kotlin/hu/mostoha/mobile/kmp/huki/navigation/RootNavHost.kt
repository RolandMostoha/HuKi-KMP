package hu.mostoha.mobile.kmp.huki.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.mostoha.mobile.kmp.huki.ui.features.gpxcollection.GpxCollectionScreen
import hu.mostoha.mobile.kmp.huki.ui.features.gpxtutorial.GpxTutorialScreen
import hu.mostoha.mobile.kmp.huki.ui.features.locationiq.LocationIqScreen
import hu.mostoha.mobile.kmp.huki.ui.features.main.MainScreen
import hu.mostoha.mobile.kmp.huki.ui.features.menu.MenuScreen
import hu.mostoha.mobile.kmp.huki.util.AnimationConstants.NAVIGATION_TRANSITION_DURATION
import hu.mostoha.mobile.kmp.huki.util.millis

private object Routes {
    const val MAIN = "main"
    const val MENU = "menu"
    const val GPX_COLLECTION = "gpx_collection"
    const val GPX_TUTORIAL = "gpx_tutorial"
    const val LOCATION_IQ = "location_iq"
    const val OPEN_GPX_URI_KEY = "open_gpx_uri"
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
        composable(Routes.MAIN) { entry ->
            val openGpxUri by entry.savedStateHandle
                .getStateFlow<String?>(Routes.OPEN_GPX_URI_KEY, null)
                .collectAsStateWithLifecycle()
            MainScreen(
                onMenuClicked = { navController.navigate(Routes.MENU) },
                onLocationIqClicked = { navController.navigate(Routes.LOCATION_IQ) },
                onGpxCollectionClicked = { navController.navigate(Routes.GPX_COLLECTION) },
                openGpxUri = openGpxUri,
                onOpenGpxConsumed = { entry.savedStateHandle[Routes.OPEN_GPX_URI_KEY] = null },
            )
        }
        composable(Routes.MENU) {
            MenuScreen(
                onBack = { navController.popBackStack() },
                onGpxCollectionClicked = { navController.navigate(Routes.GPX_COLLECTION) },
                onLocationIqClicked = { navController.navigate(Routes.LOCATION_IQ) },
            )
        }
        composable(Routes.GPX_COLLECTION) {
            GpxCollectionScreen(
                onBack = { navController.popBackStack() },
                onOpenTutorial = { navController.navigate(Routes.GPX_TUTORIAL) },
                onOpenGpx = { uri ->
                    navController.getBackStackEntry(Routes.MAIN).savedStateHandle[Routes.OPEN_GPX_URI_KEY] = uri
                    navController.popBackStack(Routes.MAIN, inclusive = false)
                },
            )
        }
        composable(Routes.GPX_TUTORIAL) {
            GpxTutorialScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.LOCATION_IQ) {
            LocationIqScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
