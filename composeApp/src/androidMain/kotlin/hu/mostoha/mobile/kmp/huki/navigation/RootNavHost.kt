package hu.mostoha.mobile.kmp.huki.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.ui.features.destinations.DestinationsScreen
import hu.mostoha.mobile.kmp.huki.ui.features.gpxcollection.GpxCollectionScreen
import hu.mostoha.mobile.kmp.huki.ui.features.gpxtutorial.GpxTutorialScreen
import hu.mostoha.mobile.kmp.huki.ui.features.locationiq.LocationIqScreen
import hu.mostoha.mobile.kmp.huki.ui.features.main.MainScreen
import hu.mostoha.mobile.kmp.huki.ui.features.menu.MenuScreen
import hu.mostoha.mobile.kmp.huki.ui.features.placehistory.PlaceHistoryScreen
import hu.mostoha.mobile.kmp.huki.ui.features.settings.SettingsScreen
import hu.mostoha.mobile.kmp.huki.util.AnimationConstants.NAVIGATION_TRANSITION_DURATION
import hu.mostoha.mobile.kmp.huki.util.millis
import kotlinx.serialization.Serializable

@Serializable
private data object Main

private object Routes {
    const val MENU = "menu"
    const val SETTINGS = "settings"
    const val DESTINATIONS = "destinations"
    const val GPX_COLLECTION = "gpx_collection"
    const val GPX_TUTORIAL = "gpx_tutorial"
    const val PLACE_HISTORY = "place_history"
    const val LOCATION_IQ = "location_iq"
    const val EXTRA_GPX_URI_KEY = "extra_gpx_uri"
    const val EXTRA_PLACE_OSM_TYPE_KEY = "extra_place_osm_type"
    const val EXTRA_PLACE_OSM_ID_KEY = "extra_place_osm_id"
    const val EXTRA_DESTINATION_OSM_ID_KEY = "extra_destination_osm_id"
}

@Composable
fun RootNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Main,
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
        composable<Main> { entry ->
            val openGpxUri by entry.savedStateHandle
                .getStateFlow<String?>(Routes.EXTRA_GPX_URI_KEY, null)
                .collectAsStateWithLifecycle()
            val openPlaceOsmType by entry.savedStateHandle
                .getStateFlow<String?>(Routes.EXTRA_PLACE_OSM_TYPE_KEY, null)
                .collectAsStateWithLifecycle()
            val openPlaceOsmId by entry.savedStateHandle
                .getStateFlow<String?>(Routes.EXTRA_PLACE_OSM_ID_KEY, null)
                .collectAsStateWithLifecycle()
            val openPlace = openPlaceOsmType?.let { type ->
                openPlaceOsmId?.let { id -> OsmType.valueOf(type) to id }
            }
            val openDestinationOsmId by entry.savedStateHandle
                .getStateFlow<String?>(Routes.EXTRA_DESTINATION_OSM_ID_KEY, null)
                .collectAsStateWithLifecycle()
            MainScreen(
                onMenuClicked = { navController.navigate(Routes.MENU) },
                onLocationIqClicked = { navController.navigate(Routes.LOCATION_IQ) },
                onGpxCollectionClicked = { navController.navigate(Routes.GPX_COLLECTION) },
                onPlaceHistoryClicked = { navController.navigate(Routes.PLACE_HISTORY) },
                onDestinationsClicked = { navController.navigate(Routes.DESTINATIONS) },
                openGpxUri = openGpxUri,
                onOpenGpxConsumed = { entry.savedStateHandle[Routes.EXTRA_GPX_URI_KEY] = null },
                openPlace = openPlace,
                onOpenPlaceConsumed = {
                    entry.savedStateHandle[Routes.EXTRA_PLACE_OSM_TYPE_KEY] = null
                    entry.savedStateHandle[Routes.EXTRA_PLACE_OSM_ID_KEY] = null
                },
                openDestinationOsmId = openDestinationOsmId,
                onOpenDestinationConsumed = { entry.savedStateHandle[Routes.EXTRA_DESTINATION_OSM_ID_KEY] = null },
            )
        }
        composable(Routes.MENU) {
            MenuScreen(
                onBack = { navController.popBackStack() },
                onSettingsClicked = { navController.navigate(Routes.SETTINGS) },
                onDestinationsClicked = { navController.navigate(Routes.DESTINATIONS) },
                onGpxCollectionClicked = { navController.navigate(Routes.GPX_COLLECTION) },
                onPlaceHistoryClicked = { navController.navigate(Routes.PLACE_HISTORY) },
                onLocationIqClicked = { navController.navigate(Routes.LOCATION_IQ) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.DESTINATIONS) {
            DestinationsScreen(
                onBack = { navController.popBackStack() },
                onShowOnMap = { destination ->
                    navController.getBackStackEntry<Main>().savedStateHandle[Routes.EXTRA_DESTINATION_OSM_ID_KEY] =
                        destination.osmId
                    navController.popBackStack<Main>(inclusive = false)
                },
            )
        }
        composable(Routes.GPX_COLLECTION) {
            GpxCollectionScreen(
                onBack = { navController.popBackStack() },
                onOpenTutorial = { navController.navigate(Routes.GPX_TUTORIAL) },
                onOpenGpx = { uri ->
                    navController.getBackStackEntry<Main>().savedStateHandle[Routes.EXTRA_GPX_URI_KEY] = uri
                    navController.popBackStack<Main>(inclusive = false)
                },
            )
        }
        composable(Routes.PLACE_HISTORY) {
            PlaceHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenPlace = { osmType, osmId ->
                    val mainStateHandle = navController.getBackStackEntry<Main>().savedStateHandle
                    mainStateHandle[Routes.EXTRA_PLACE_OSM_TYPE_KEY] = osmType.name
                    mainStateHandle[Routes.EXTRA_PLACE_OSM_ID_KEY] = osmId
                    navController.popBackStack<Main>(inclusive = false)
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
