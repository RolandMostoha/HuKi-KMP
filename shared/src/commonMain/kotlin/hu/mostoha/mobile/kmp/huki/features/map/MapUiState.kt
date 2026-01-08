package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.util.MapConstants

data class MapUiState(
    val zoomLevel: Double = MapConstants.HUNGARY_ZOOM_LEVEL,
    val latitude: Double = MapConstants.HUNGARY_CENTER_LATITUDE,
    val longitude: Double = MapConstants.HUNGARY_CENTER_LONGITUDE,
) {
    companion object {
        val Default = MapUiState()
    }
}
