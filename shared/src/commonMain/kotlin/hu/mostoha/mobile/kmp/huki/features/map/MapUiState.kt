package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer

data class MapUiState(
    val baseLayer: BaseLayer = BaseLayer.OUTDOORS,
    val hikingLayerVisible: Boolean = true,
    val gpxLayerVisible: Boolean = false,
) {
    companion object {
        val Default = MapUiState()
    }
}
