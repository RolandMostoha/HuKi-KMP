package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.Place

sealed interface MainUiEvents {
    /**
     * General events
     */
    data object SheetDismissed : MainUiEvents
    data object AlertDismissed : MainUiEvents

    /**
     * Search events
     */
    data object SearchClicked : MainUiEvents
    data class SearchPlaceSelected(val place: Place) : MainUiEvents

    /**
     * My location events
     */
    data object MyLocationClicked : MainUiEvents
    data object FollowingDisabled : MainUiEvents

    /**
     * Layers events
     */
    data object LayersClicked : MainUiEvents
    data class BaseLayerSelected(val baseLayer: BaseLayer) : MainUiEvents
    data object HikingLayerSelected : MainUiEvents

    /**
     * GPX events
     */
    data object GpxLayerSelected : MainUiEvents
    data object GpxStartNavigationClicked : MainUiEvents
    data class GpxRouteClicked(val gpxDetails: GpxDetails) : MainUiEvents
    data object GpxCloseClicked : MainUiEvents
    data class GpxFileSelected(val uri: String) : MainUiEvents
}
