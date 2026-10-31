package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.features.map.LandscapesGrouping

data class MainSession(
    val camera: CameraPosition,
    val cameraPadding: ContentPadding?,
    val myLocationStatus: MyLocationStatus,
    val hikingLayerVisible: Boolean,
    val feature: FeatureSession?,
)

sealed interface FeatureSession {
    data class Gpx(
        val fileUri: String,
        val isReversed: Boolean,
        val isLayerVisible: Boolean,
        val isRouteVisible: Boolean,
        val isAllDistancesVisible: Boolean,
        val isSlopeColorsVisible: Boolean,
        val isSheetVisible: Boolean,
    ) : FeatureSession

    data class Okt(
        val type: OktType,
        val selectedSectionId: String,
        val reversedSectionIds: Set<String>,
        val isSheetVisible: Boolean,
    ) : FeatureSession

    data class Landscapes(
        val grouping: LandscapesGrouping,
        val selectedLandscapeId: String?,
        val selectedCategoryId: String?,
        val selectedDestinationId: String?,
        val isSheetVisible: Boolean,
    ) : FeatureSession

    data class RoutePlanner(val session: RoutePlannerSession) : FeatureSession
}

data class RoutePlannerSession(
    val routeProfile: RoutePlannerProfile,
    val waypoints: List<RoutePlannerWaypoint>,
)
