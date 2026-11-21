package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.analytics.SearchSection
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerMode
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerSession
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet

sealed interface MainUiEvents {
    /**
     * General events
     */
    data object SheetDismissed : MainUiEvents
    data class SheetSwipeDismissed(val sheet: Sheet) : MainUiEvents
    data object AlertDismissed : MainUiEvents

    /**
     * Search events
     */
    data object SearchClicked : MainUiEvents

    data object SearchBarSupportClicked : MainUiEvents
    data class SearchPlaceSelected(val place: Place) : MainUiEvents
    data class SearchRecentPlaceSelected(val place: Place) : MainUiEvents
    data class SearchDestinationSelected(val destination: Destination) : MainUiEvents
    data class SearchResultPlaceHistorySelected(val place: Place) : MainUiEvents
    data class SearchResultDestinationSelected(val destination: Destination) : MainUiEvents
    data class SearchSeeAllClicked(val section: SearchSection) : MainUiEvents
    data class DestinationSelected(val id: String) : MainUiEvents
    data class DestinationRouteSelected(val id: String) : MainUiEvents
    data class HistoryPlaceSelected(
        val osmType: OsmType,
        val osmId: String,
    ) : MainUiEvents

    /**
     * Discover events
     */
    data object DiscoverClicked : MainUiEvents
    data class HikeRecommendationClicked(val recommendation: HikeRecommendation) : MainUiEvents
    data object DiscoverBrowseDestinationsClicked : MainUiEvents
    data object HikeRecommendationsInfoClicked : MainUiEvents
    data object HikeRecommendationsGpxGuideClicked : MainUiEvents

    /**
     * Map events
     */
    data class MapCameraChanged(val cameraPosition: CameraPosition) : MainUiEvents

    /**
     * Place Details events
     */
    data class MapLongClicked(val location: Location) : MainUiEvents
    data object PlaceDetailsCloseClicked : MainUiEvents
    data object PlaceDetailsRoutePlanClicked : MainUiEvents
    data class RouteStopClicked(val stop: PlaceDetails) : MainUiEvents
    data object PlaceDetailsMapsNavigationClicked : MainUiEvents

    /**
     * Route Planner events
     */
    data object RoutePlannerClicked : MainUiEvents
    data object RoundTripWizardClicked : MainUiEvents
    data class RoutePlanUpdated(
        val routePlan: RoutePlan?,
        val markers: List<GpxWaypoint>,
        val isAwaitingRoutePlan: Boolean = false,
        val isRoutePlanLoading: Boolean = false,
        val mode: RoutePlannerMode = RoutePlannerMode.PLANNER,
    ) : MainUiEvents
    data class RoutePlannerStateChanged(val session: RoutePlannerSession) : MainUiEvents
    data class RoutePlannerPickOnMapChanged(val isPickingOnMap: Boolean) : MainUiEvents
    data class RoutePlannerSelectedStopChanged(val location: Location?) : MainUiEvents
    data class RoutePlannerMoveStopChanged(val isMovingStop: Boolean) : MainUiEvents
    data class RoutePlanMarkerDragged(
        val from: Location,
        val to: Location,
    ) : MainUiEvents
    data class RoutePlanMarkerClicked(val location: Location) : MainUiEvents
    data object MapClicked : MainUiEvents

    data object RoutePlannerHintDismissed : MainUiEvents

    /**
     * My location events
     */
    data object MyLocationClicked : MainUiEvents
    data object MyLocationLongClicked : MainUiEvents
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
    data object BikeLayerSelected : MainUiEvents

    /**
     * Map label scale events
     */
    data object MapLabelScaleClicked : MainUiEvents
    data class MapLabelScaleChanged(val factor: Float) : MainUiEvents
    data class MapLabelScaleCommitted(val factor: Float) : MainUiEvents
}
