package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.mapper.toGpxWaypoints

data class MapUiState(
    val baseLayer: BaseLayer = BaseLayer.OUTDOORS,
    val hikingLayerVisible: Boolean = true,
    val gpxLayerVisible: Boolean = false,
    val gpxRouteVisible: Boolean = true,
    val gpxDetails: GpxDetails? = null,
    val placeDetails: PlaceDetails? = null,
    val routePlan: RoutePlan? = null,
    val routePlanWaypoints: List<GpxWaypoint> = emptyList(),
    val allDistancesVisible: Boolean = false,
    val distanceInfoWindows: List<DistanceInfoWindowData> = emptyList(),
) {
    val routePlanMarkers: List<GpxWaypoint>
        get() = routePlan?.toGpxWaypoints() ?: routePlanWaypoints

    companion object {
        val Default = MapUiState()
    }
}
