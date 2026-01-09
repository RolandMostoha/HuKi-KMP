package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.util.MapConstants

data class MapUiState(
    val cameraPosition: CameraPosition = CameraPosition(
        zoom = MapConstants.HUNGARY_ZOOM_LEVEL,
        latitude = MapConstants.HUNGARY_CENTER_LATITUDE,
        longitude = MapConstants.HUNGARY_CENTER_LONGITUDE,
    ),
) {
    companion object {
        val Default = MapUiState()
    }
}
