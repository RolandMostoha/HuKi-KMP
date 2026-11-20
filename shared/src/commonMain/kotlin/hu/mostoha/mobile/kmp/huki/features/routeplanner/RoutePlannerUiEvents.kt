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

    data object AddEmptyStopClicked : RoutePlannerUiEvents
    data class StopClicked(val waypointId: String) : RoutePlannerUiEvents
    data class StopMoveClicked(val waypointId: String) : RoutePlannerUiEvents
    data class StopDragged(
        val from: Location,
        val to: Location,
    ) : RoutePlannerUiEvents
    data class StopMarkerClicked(val location: Location) : RoutePlannerUiEvents
    data object StopDeselected : RoutePlannerUiEvents

    data class StopReplaceClicked(val waypointId: String) : RoutePlannerUiEvents
    data class StopRenameClicked(val waypointId: String) : RoutePlannerUiEvents
    data class StopRenamed(
        val waypointId: String,
        val name: String,
        val comment: String,
    ) : RoutePlannerUiEvents

    data object StopRenameDismissed : RoutePlannerUiEvents
    data class AddStopFromSearchClicked(val waypointId: String? = null) : RoutePlannerUiEvents
    data object WaypointSearchDismissed : RoutePlannerUiEvents
    data class SearchPlaceAdded(val place: Place) : RoutePlannerUiEvents
    data class SearchDestinationAdded(val destination: Destination) : RoutePlannerUiEvents
    data object MyLocationAdded : RoutePlannerUiEvents
    data class PickOnMapClicked(val waypointId: String? = null) : RoutePlannerUiEvents
    data object RoundTripClicked : RoutePlannerUiEvents
    data object RetryClicked : RoutePlannerUiEvents
    data object SaveRouteClicked : RoutePlannerUiEvents
    data object CloseClicked : RoutePlannerUiEvents
    data object GraphhopperAttributionClicked : RoutePlannerUiEvents
}
