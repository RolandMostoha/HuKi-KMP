package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails

data class MapUiState(
    val baseLayer: BaseLayer = BaseLayer.OUTDOORS,
    val hikingLayerVisible: Boolean = true,
    val gpxLayerVisible: Boolean = false,
    val gpxRouteVisible: Boolean = true,
    val gpxDetails: GpxDetails? = null,
    val allDistancesVisible: Boolean = false,
    val distanceInfoWindows: List<DistanceInfoWindowData> = emptyList(),
) {
    companion object {
        val Default = MapUiState()
    }
}
