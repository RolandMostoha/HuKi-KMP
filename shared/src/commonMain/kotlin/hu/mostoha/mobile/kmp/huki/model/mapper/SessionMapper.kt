package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.main.MainUiState
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesGrouping
import hu.mostoha.mobile.kmp.huki.model.data.CameraSnapshot
import hu.mostoha.mobile.kmp.huki.model.data.FeatureSnapshot
import hu.mostoha.mobile.kmp.huki.model.data.MainSessionSnapshot
import hu.mostoha.mobile.kmp.huki.model.data.WaypointSnapshot
import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.FeatureSession
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MainSession
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerSession
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet

private const val MY_LOCATION_STATUS_NOT_AVAILABLE = "not_available"
private const val MY_LOCATION_STATUS_DEFAULT = "default"
private const val MY_LOCATION_STATUS_FOLLOWING = "following"
private const val MY_LOCATION_STATUS_LIVE_COMPASS = "live_compass"

fun MainUiState.toMainSession(
    camera: CameraPosition,
    cameraPadding: ContentPadding?,
    routePlannerSession: RoutePlannerSession?,
): MainSession =
    MainSession(
        camera = camera,
        cameraPadding = cameraPadding,
        myLocationStatus = myLocationState.myLocationStatus,
        hikingLayerVisible = mapUiState.hikingLayerVisible,
        feature = toFeatureSession(routePlannerSession),
    )

private fun MainUiState.toFeatureSession(routePlannerSession: RoutePlannerSession?): FeatureSession? {
    val gpxDetails = mapUiState.gpxDetails
    val okt = mapUiState.okt
    val landscapes = mapUiState.landscapes
    return when {
        sheet is Sheet.RoutePlanner && routePlannerSession != null -> FeatureSession.RoutePlanner(routePlannerSession)
        gpxDetails != null -> FeatureSession.Gpx(
            fileUri = gpxDetails.fileUri,
            isReversed = gpxDetails.isReversed,
            isLayerVisible = mapUiState.gpxLayerVisible,
            isRouteVisible = mapUiState.gpxRouteVisible,
            isAllDistancesVisible = mapUiState.allDistancesVisible,
            isSlopeColorsVisible = mapUiState.gpxSlopeColorsVisible,
            isSheetVisible = sheet is Sheet.Gpx,
        )
        okt != null -> FeatureSession.Okt(
            type = okt.type,
            selectedSectionId = okt.selectedSectionId,
            reversedSectionIds = okt.reversedSectionIds,
            isSheetVisible = sheet == Sheet.Okt,
        )
        landscapes != null -> FeatureSession.Landscapes(
            grouping = landscapes.grouping,
            selectedLandscapeId = landscapes.selectedLandscapeId,
            selectedCategoryId = landscapes.selectedCategoryId,
            selectedDestinationId = landscapes.selectedDestinationId,
            isSheetVisible = sheet == Sheet.Landscapes,
        )
        else -> null
    }
}

fun MainSession.toMainSessionSnapshot(): MainSessionSnapshot =
    MainSessionSnapshot(
        camera = CameraSnapshot(
            latitude = camera.location.latitude,
            longitude = camera.location.longitude,
            zoom = camera.zoom,
            bearing = camera.bearing,
            pitch = camera.pitch,
            padding = cameraPadding?.name,
        ),
        myLocationStatus = when (myLocationStatus) {
            MyLocationStatus.NotAvailable -> MY_LOCATION_STATUS_NOT_AVAILABLE
            MyLocationStatus.Default -> MY_LOCATION_STATUS_DEFAULT
            MyLocationStatus.Following -> MY_LOCATION_STATUS_FOLLOWING
            MyLocationStatus.FollowingLiveCompass -> MY_LOCATION_STATUS_LIVE_COMPASS
        },
        hikingLayerVisible = hikingLayerVisible,
        feature = feature?.toFeatureSnapshot(),
    )

private fun FeatureSession.toFeatureSnapshot(): FeatureSnapshot =
    when (this) {
        is FeatureSession.Gpx -> FeatureSnapshot.Gpx(
            fileUri = fileUri,
            isReversed = isReversed,
            isLayerVisible = isLayerVisible,
            isRouteVisible = isRouteVisible,
            isAllDistancesVisible = isAllDistancesVisible,
            isSlopeColorsVisible = isSlopeColorsVisible,
            isSheetVisible = isSheetVisible,
        )
        is FeatureSession.Okt -> FeatureSnapshot.Okt(
            oktType = type.name,
            selectedSectionId = selectedSectionId,
            reversedSectionIds = reversedSectionIds,
            isSheetVisible = isSheetVisible,
        )
        is FeatureSession.Landscapes -> FeatureSnapshot.Landscapes(
            grouping = grouping.name,
            selectedLandscapeId = selectedLandscapeId,
            selectedCategoryId = selectedCategoryId,
            selectedDestinationId = selectedDestinationId,
            isSheetVisible = isSheetVisible,
        )
        is FeatureSession.RoutePlanner -> FeatureSnapshot.RoutePlanner(
            routeProfile = session.routeProfile.name,
            waypoints = session.waypoints.map { it.toWaypointSnapshot() },
        )
    }

/**
 * Returns `null` for a session saved by another app version or with values this version no longer knows.
 */
fun MainSessionSnapshot.toMainSession(): MainSession? {
    if (version != MainSessionSnapshot.VERSION) return null
    val status = when (myLocationStatus) {
        MY_LOCATION_STATUS_NOT_AVAILABLE -> MyLocationStatus.NotAvailable
        MY_LOCATION_STATUS_DEFAULT -> MyLocationStatus.Default
        MY_LOCATION_STATUS_FOLLOWING -> MyLocationStatus.Following
        MY_LOCATION_STATUS_LIVE_COMPASS -> MyLocationStatus.FollowingLiveCompass
        else -> return null
    }
    return MainSession(
        camera = CameraPosition(
            location = Location(camera.latitude, camera.longitude),
            zoom = camera.zoom,
            bearing = camera.bearing,
            pitch = camera.pitch,
        ),
        cameraPadding = ContentPadding.entries.firstOrNull { it.name == camera.padding },
        myLocationStatus = status,
        hikingLayerVisible = hikingLayerVisible,
        feature = feature?.let { it.toFeatureSession() ?: return null },
    )
}

private fun FeatureSnapshot.toFeatureSession(): FeatureSession? =
    when (this) {
        is FeatureSnapshot.Gpx -> FeatureSession.Gpx(
            fileUri = fileUri,
            isReversed = isReversed,
            isLayerVisible = isLayerVisible,
            isRouteVisible = isRouteVisible,
            isAllDistancesVisible = isAllDistancesVisible,
            isSlopeColorsVisible = isSlopeColorsVisible,
            isSheetVisible = isSheetVisible,
        )
        is FeatureSnapshot.Okt -> OktType.entries.firstOrNull { it.name == oktType }?.let { oktType ->
            FeatureSession.Okt(
                type = oktType,
                selectedSectionId = selectedSectionId,
                reversedSectionIds = reversedSectionIds,
                isSheetVisible = isSheetVisible,
            )
        }
        is FeatureSnapshot.Landscapes -> LandscapesGrouping.entries.firstOrNull { it.name == grouping }?.let {
            FeatureSession.Landscapes(
                grouping = it,
                selectedLandscapeId = selectedLandscapeId,
                selectedCategoryId = selectedCategoryId,
                selectedDestinationId = selectedDestinationId,
                isSheetVisible = isSheetVisible,
            )
        }
        is FeatureSnapshot.RoutePlanner -> RoutePlannerProfile.entries.firstOrNull { it.name == routeProfile }
            ?.let { profile ->
                FeatureSession.RoutePlanner(
                    RoutePlannerSession(
                        routeProfile = profile,
                        waypoints = waypoints.map { it.toRoutePlannerWaypoint() },
                    ),
                )
            }
    }

fun RoutePlannerWaypoint.toWaypointSnapshot(): WaypointSnapshot {
    val isMyLocation = name == StringDesc.Resource(SharedRes.strings.route_planner_my_location)
    return WaypointSnapshot(
        id = id,
        isPlaceName = placeName != null && name == StringDesc.Raw(placeName),
        isMyLocation = isMyLocation,
        placeName = placeName,
        latitude = location?.latitude,
        longitude = location?.longitude,
    )
}

/**
 * A coordinate stop is named by its location again.
 */
fun WaypointSnapshot.toRoutePlannerWaypoint(): RoutePlannerWaypoint {
    val location = if (latitude != null && longitude != null) Location(latitude, longitude) else null
    val restoredName = when {
        isMyLocation -> StringDesc.Resource(SharedRes.strings.route_planner_my_location)
        isPlaceName && placeName != null -> StringDesc.Raw(placeName)
        else -> location?.toRoutePlannerWaypoint()?.name
    }
    return RoutePlannerWaypoint(id = id, name = restoredName, placeName = placeName, location = location)
}
