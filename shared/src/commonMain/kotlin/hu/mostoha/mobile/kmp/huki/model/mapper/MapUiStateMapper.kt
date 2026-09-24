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
