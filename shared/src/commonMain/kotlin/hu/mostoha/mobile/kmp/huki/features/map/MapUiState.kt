package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition

data class MapUiState(val cameraPosition: CameraPosition? = null) {
    companion object {
        val Default = MapUiState()
    }
}
