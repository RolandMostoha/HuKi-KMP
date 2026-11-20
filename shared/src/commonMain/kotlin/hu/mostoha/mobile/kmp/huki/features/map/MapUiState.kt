package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MapLabelScale
import hu.mostoha.mobile.kmp.huki.model.domain.OverlayLayer
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import hu.mostoha.mobile.kmp.huki.model.mapper.toGpxWaypoints
import hu.mostoha.mobile.kmp.huki.util.distanceBetween

data class MapUiState(
    val baseLayer: BaseLayer = UserPreferences.DEFAULTS.baseLayer,
    val rotationEnabled: Boolean = UserPreferences.DEFAULTS.mapRotationEnabled,
    val labelScale: MapLabelScale = UserPreferences.DEFAULTS.mapLabelScale,
    val hikingLayerVisible: Boolean = true,
    val bikeLayerVisible: Boolean = false,
    val gpxLayerVisible: Boolean = false,
    val gpxRouteVisible: Boolean = true,
    val gpxDetails: GpxDetails? = null,
    val placeDetails: PlaceDetails? = null,
    val routePlan: RoutePlan? = null,
    val routePlanWaypoints: List<GpxWaypoint> = emptyList(),
    val selectedRoutePlanStop: Location? = null,
    /**
     * A replan can snap a moved stop back onto the same point, an unchanged plan, so markers reset when it ends.
     */
    val isRoutePlanLoading: Boolean = false,
    /**
     * Changed stops wait for their new plan, which brings its own camera, so the sheet's resizing meanwhile must not.
     */
    val isRoutePlanPending: Boolean = false,
    val allDistancesVisible: Boolean = false,
    val gpxSlopeColorsVisible: Boolean = false,
    val distanceInfoWindows: List<DistanceInfoWindowData> = emptyList(),
    val okt: OktUiState? = null,
    val landscapes: LandscapesUiState? = null,
    val routeStops: List<PlaceDetails> = emptyList(),
) {

    val isGpxShown: Boolean
        get() = gpxDetails != null && gpxLayerVisible

    val routePlanMarkers: List<GpxWaypoint>
        get() = routePlan?.toGpxWaypoints() ?: routePlanWaypoints

    /**
     * The nearest marker, since a planned route snaps its markers onto the path, away from the stop's own location.
     */
    val selectedRoutePlanMarker: GpxWaypoint?
        get() {
            val stop = selectedRoutePlanStop ?: return null
            return routePlanMarkers.minByOrNull { it.location.distanceBetween(stop) }
        }

    /**
     * In drawing order, bike routes on top of the hiking trails. Open Landscapes hide the bike routes too.
     */
    val visibleOverlays: List<OverlayLayer>
        get() = buildList {
            if (hikingLayerVisible) add(OverlayLayer.TURISTAUTAK)
            if (bikeLayerVisible && landscapes == null) add(OverlayLayer.WAYMARKED_CYCLING)
        }

    companion object {
        val Default = MapUiState()
    }
}
