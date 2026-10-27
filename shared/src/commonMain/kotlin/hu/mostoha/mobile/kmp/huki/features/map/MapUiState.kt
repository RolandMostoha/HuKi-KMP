package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.MapLabelScale
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import hu.mostoha.mobile.kmp.huki.model.mapper.toGpxWaypoints

data class MapUiState(
    val baseLayer: BaseLayer = UserPreferences.DEFAULTS.baseLayer,
    val rotationEnabled: Boolean = UserPreferences.DEFAULTS.mapRotationEnabled,
    val labelScale: MapLabelScale = UserPreferences.DEFAULTS.mapLabelScale,
    val hikingLayerVisible: Boolean = true,
    val gpxLayerVisible: Boolean = false,
    val gpxRouteVisible: Boolean = true,
    val gpxDetails: GpxDetails? = null,
    val placeDetails: PlaceDetails? = null,
    val routePlan: RoutePlan? = null,
    val routePlanWaypoints: List<GpxWaypoint> = emptyList(),
    val allDistancesVisible: Boolean = false,
    val gpxSlopeColorsVisible: Boolean = false,
    val distanceInfoWindows: List<DistanceInfoWindowData> = emptyList(),
    val okt: OktUiState? = null,
    val landscapes: LandscapesUiState? = null,
    val routeStops: List<PlaceDetails> = emptyList(),
) {

    val routePlanMarkers: List<GpxWaypoint>
        get() = routePlan?.toGpxWaypoints() ?: routePlanWaypoints

    companion object {
        val Default = MapUiState()
    }
}
