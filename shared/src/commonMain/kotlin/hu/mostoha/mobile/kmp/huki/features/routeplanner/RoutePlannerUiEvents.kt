package hu.mostoha.mobile.kmp.huki.features.routeplanner

import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile

sealed interface RoutePlannerUiEvents {
    data class ProfileSelected(val routeProfile: RoutePlannerProfile) : RoutePlannerUiEvents
    data class PlaceAdded(val place: Place) : RoutePlannerUiEvents
    data class LocationAdded(val location: Location) : RoutePlannerUiEvents
    data class WaypointRemoved(val id: String) : RoutePlannerUiEvents
    data class WaypointMoved(
        val fromIndex: Int,
        val toIndex: Int,
    ) : RoutePlannerUiEvents

    data class AddStopFromSearchClicked(val waypointId: String? = null) : RoutePlannerUiEvents
    data object WaypointSearchDismissed : RoutePlannerUiEvents
    data class SearchPlaceAdded(val place: Place) : RoutePlannerUiEvents
    data class SearchDestinationAdded(val destination: Destination) : RoutePlannerUiEvents
    data object MyLocationAdded : RoutePlannerUiEvents
    data object PickOnMapClicked : RoutePlannerUiEvents
    data object RoundTripClicked : RoutePlannerUiEvents
    data object RetryClicked : RoutePlannerUiEvents
    data object SaveRouteClicked : RoutePlannerUiEvents
    data object CloseClicked : RoutePlannerUiEvents
}
