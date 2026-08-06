package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.analytics.GpxSource
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.GpxMapsNavigationType
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place

sealed interface MainUiEvents {
    /**
     * General events
     */
    data object SheetDismissed : MainUiEvents
    data object AlertDismissed : MainUiEvents

    /**
     * Search events
     */
    data object SearchClicked : MainUiEvents
    data class SearchPlaceSelected(val place: Place) : MainUiEvents
    data class SearchRecentPlaceSelected(val place: Place) : MainUiEvents
    data class SearchDestinationSelected(val destination: Destination) : MainUiEvents
    data class SearchResultPlaceHistorySelected(val place: Place) : MainUiEvents
    data class SearchResultDestinationSelected(val destination: Destination) : MainUiEvents
    data class DestinationSelected(val osmId: String) : MainUiEvents
    data class HistoryPlaceSelected(
        val osmType: OsmType,
        val osmId: String,
    ) : MainUiEvents

    /**
     * Place Details events
     */
    data class MapLongClicked(val location: Location) : MainUiEvents
    data object PlaceDetailsCloseClicked : MainUiEvents
    data object PlaceDetailsRoutePlanClicked : MainUiEvents
    data object PlaceDetailsMapsNavigationClicked : MainUiEvents

    /**
     * My location events
     */
    data object MyLocationClicked : MainUiEvents
    data object MyLocationReceived : MainUiEvents
    data object FollowingDisabled : MainUiEvents
    data object CompassClicked : MainUiEvents
    data object ZoomInClicked : MainUiEvents
    data object ZoomOutClicked : MainUiEvents

    /**
     * Layers events
     */
    data object LayersClicked : MainUiEvents
    data class BaseLayerSelected(val baseLayer: BaseLayer) : MainUiEvents
    data object HikingLayerSelected : MainUiEvents

    /**
     * GPX events
     */
    data object GpxLayerSelected : MainUiEvents
    data object GpxStartNavigationClicked : MainUiEvents
    data class GpxMapsNavigationClicked(val type: GpxMapsNavigationType) : MainUiEvents
    data object GpxCloseClicked : MainUiEvents
    data class GpxFileSelected(
        val uri: String,
        val source: GpxSource = GpxSource.FILES,
    ) : MainUiEvents
    data class GpxFileReopened(val uri: String) : MainUiEvents
    data object GpxRouteVisibilityToggled : MainUiEvents
    data object GpxDistancesVisibilityToggled : MainUiEvents
    data object GpxOverviewClicked : MainUiEvents
    data class GpxWaypointClicked(val waypoint: GpxWaypoint) : MainUiEvents
    data object DistanceInfoWindowDismissed : MainUiEvents
}
