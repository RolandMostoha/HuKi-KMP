package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.MapConfiguration

data class MapUiState(
    val cameraPosition: CameraPosition = CameraPosition(
        zoom = MapConfiguration.HUNGARY_ZOOM_LEVEL,
        location = Location(
            latitude = MapConfiguration.HUNGARY_CENTER_LATITUDE,
            longitude = MapConfiguration.HUNGARY_CENTER_LONGITUDE,
        ),
        bearing = 0.0,
        pitch = 0.0,
    ),
) {
    companion object {
        val Default = MapUiState()
    }
}
