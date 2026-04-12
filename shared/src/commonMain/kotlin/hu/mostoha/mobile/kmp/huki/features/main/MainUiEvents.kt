package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer

sealed interface MainUiEvents {
    data object MyLocationClicked : MainUiEvents
    data object GpxStartNavigationClicked : MainUiEvents
    data object GpxRouteClicked : MainUiEvents
    data object GpxCloseClicked : MainUiEvents
    data object AlertDismissed : MainUiEvents
    data object FollowingDisabled : MainUiEvents
    data object LayersClicked : MainUiEvents
    data object LayersDismissed : MainUiEvents
    data class BaseLayerSelected(val baseLayer: BaseLayer) : MainUiEvents
    data object HikingLayerSelected : MainUiEvents
    data object GpxLayerSelected : MainUiEvents
    data class GpxFileSelected(val uri: String) : MainUiEvents
}
