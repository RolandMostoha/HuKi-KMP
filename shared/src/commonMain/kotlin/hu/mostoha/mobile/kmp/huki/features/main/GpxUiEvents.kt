package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.analytics.GpxSource
import hu.mostoha.mobile.kmp.huki.model.domain.GpxMapsNavigationType
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint

sealed interface GpxUiEvents : MainUiEvents {
    data object GpxLayerSelected : GpxUiEvents
    data object GpxStartNavigationClicked : GpxUiEvents
    data class GpxMapsNavigationClicked(val type: GpxMapsNavigationType) : GpxUiEvents
    data object GpxCloseClicked : GpxUiEvents
    data object GpxShareClicked : GpxUiEvents
    data class GpxFileSelected(
        val uri: String,
        val source: GpxSource = GpxSource.FILES,
    ) : GpxUiEvents
    data class GpxFileReopened(val uri: String) : GpxUiEvents
    data class RoutePlanGpxSaved(val uri: String) : GpxUiEvents
    data object GpxRouteVisibilityToggled : GpxUiEvents
    data object GpxDistancesVisibilityToggled : GpxUiEvents
    data object GpxSlopeColorsToggled : GpxUiEvents
    data object GpxDirectionReversed : GpxUiEvents
    data object GpxOverviewClicked : GpxUiEvents
    data class GpxWaypointClicked(val waypoint: GpxWaypoint) : GpxUiEvents
    data object DistanceInfoWindowDismissed : GpxUiEvents
}
