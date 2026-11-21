package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer

fun MapUiState.withoutGpx(): MapUiState =
    copy(
        gpxDetails = null,
        gpxLayerVisible = false,
        gpxRouteVisible = true,
        allDistancesVisible = false,
        distanceInfoWindows = emptyList(),
    )

/**
 * The hiking layer is hidden while Landscapes are open, so closing them brings it back, except on TuHu.
 */
fun MapUiState.withoutLandscapes(): MapUiState =
    if (landscapes == null) this else copy(landscapes = null, hikingLayerVisible = baseLayer != BaseLayer.TUHU)

/**
 * TuHu already draws the marked trails, so selecting it hides the hiking layer and leaving it always shows it again.
 */
fun MapUiState.withBaseLayer(baseLayer: BaseLayer): MapUiState {
    val hikingLayerVisible = when {
        landscapes != null -> hikingLayerVisible
        this.baseLayer != BaseLayer.TUHU && baseLayer == BaseLayer.TUHU -> false
        this.baseLayer == BaseLayer.TUHU && baseLayer != BaseLayer.TUHU -> true
        else -> hikingLayerVisible
    }
    return copy(baseLayer = baseLayer, hikingLayerVisible = hikingLayerVisible)
}

/**
 * Clears everything the route planner draws on the map, including a replan still in flight.
 */
fun MapUiState.withoutRoutePlan(): MapUiState =
    copy(
        routePlan = null,
        routePlanWaypoints = emptyList(),
        selectedRoutePlanStop = null,
        isRoutePlanLoading = false,
        isRoutePlanPending = false,
    )
