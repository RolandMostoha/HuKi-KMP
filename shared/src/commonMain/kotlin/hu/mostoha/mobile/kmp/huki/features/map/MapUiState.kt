package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails

data class MapUiState(
    val baseLayer: BaseLayer = BaseLayer.OUTDOORS,
    val hikingLayerVisible: Boolean = true,
    val gpxLayerVisible: Boolean = false,
    val gpxDetails: GpxDetails? = null,
) {
    companion object {
        val Default = MapUiState()
    }
}
