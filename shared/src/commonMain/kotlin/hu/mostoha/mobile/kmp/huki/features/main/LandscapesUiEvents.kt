package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.Destination

sealed interface LandscapesUiEvents : MainUiEvents {
    data object LandscapesShowClicked : LandscapesUiEvents
    data object CategoriesShowClicked : LandscapesUiEvents
    data class LandscapeClicked(val landscapeId: String) : LandscapesUiEvents
    data class CategoryClicked(val categoryId: String) : LandscapesUiEvents
    data object LandscapesShowAllClicked : LandscapesUiEvents
    data class LandscapesDestinationClicked(val destination: Destination) : LandscapesUiEvents
    data object LandscapesInfoWindowDismissed : LandscapesUiEvents
    data object LandscapesInfoWindowPlaceDetailsClicked : LandscapesUiEvents
    data object LandscapesInfoWindowRouteClicked : LandscapesUiEvents
    data object LandscapesResumeClicked : LandscapesUiEvents
    data object LandscapesCloseClicked : LandscapesUiEvents
}
