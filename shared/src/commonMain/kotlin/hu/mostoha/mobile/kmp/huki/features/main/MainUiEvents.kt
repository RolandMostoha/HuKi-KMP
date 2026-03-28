package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer

sealed interface MainUiEvents {
    data object MyLocationClicked : MainUiEvents
    data object FollowingDisabled : MainUiEvents
    data object LayersClicked : MainUiEvents
    data class BaseLayerSelected(val baseLayer: BaseLayer) : MainUiEvents
    data object HikingLayerSelected : MainUiEvents
    data object GpxLayerSelected : MainUiEvents
}
