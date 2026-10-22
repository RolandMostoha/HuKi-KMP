package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.features.map.MapUiState

fun MapUiState.withoutGpx(): MapUiState =
    copy(
        gpxDetails = null,
        gpxLayerVisible = false,
        gpxRouteVisible = true,
        allDistancesVisible = false,
        distanceInfoWindows = emptyList(),
    )

/**
 * The hiking layer is hidden while Landscapes are open, so closing them brings it back.
 */
fun MapUiState.withoutLandscapes(): MapUiState =
    if (landscapes == null) this else copy(landscapes = null, hikingLayerVisible = true)
