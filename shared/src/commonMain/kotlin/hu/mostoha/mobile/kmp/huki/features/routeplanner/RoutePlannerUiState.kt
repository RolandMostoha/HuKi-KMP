package hu.mostoha.mobile.kmp.huki.features.routeplanner

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlanRequest
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.kilometers

data class RoutePlannerUiState(
    val routeProfile: RoutePlannerProfile = RoutePlannerProfile.ON_TRAILS,
    val waypoints: List<RoutePlannerWaypoint> = DEFAULT_WAYPOINTS,
    val routePlan: RoutePlan? = null,
    val isRoutePlanLoading: Boolean = false,
    val routePlanError: InfoViewData? = null,
    val isWaypointSearchVisible: Boolean = false,
    val waypointSearchTargetId: String? = null,
    val isPickingOnMap: Boolean = false,
    val myLocation: Location? = null,
) {
    val stops: List<RoutePlannerWaypoint>
        get() = waypoints.dropLast(1)

    val routeStats: RouteStats?
        get() = routePlan?.routeStats

    val isRoundTrip: Boolean
        get() = stops.size >= MIN_WAYPOINT_COUNT &&
            stops.first().location != null &&
            stops.first().location == stops.last().location

    val isRoundTripEnabled: Boolean
        get() = !isRoundTrip && !isMaxStopsReached && stops.firstOrNull()?.location != null

    val isMaxStopsReached: Boolean
        get() = stops.size >= MAX_WAYPOINT_COUNT

    val isSaveEnabled: Boolean
        get() = routePlan != null && !isRoutePlanLoading

    val isRoutePlanExpected: Boolean
        get() = routePlanRequest.waypoints.size >= MIN_WAYPOINT_COUNT

    val isStopListVisible: Boolean
        get() = routePlanError == null

    val isSaveButtonVisible: Boolean
        get() = routePlanError == null

    val routePlanMarkers: List<GpxWaypoint>
        get() = (if (isRoundTrip) stops.dropLast(1) else stops)
            .mapIndexedNotNull { index, stop ->
                stop.location?.let { location ->
                    val type = if (index == 0 && isRoundTrip) WaypointType.ROUND_TRIP else stopType(index)
                    GpxWaypoint(location, type)
                }
            }

    val routePlanRequest: RoutePlanRequest
        get() = RoutePlanRequest(
            routeProfile = routeProfile,
            waypoints = stops.mapNotNull { it.location },
        )

    fun stopType(index: Int): WaypointType =
        when {
            index == 0 -> WaypointType.START
            index == stops.lastIndex -> WaypointType.END
            else -> WaypointType.INTERMEDIATE
        }

    fun withMyLocationRangeGuard(): RoutePlannerUiState {
        val startLocation = stops.firstOrNull()
            ?.takeIf { it.id == MY_LOCATION_WAYPOINT_ID }
            ?.location
            ?: return this
        val target = stops.drop(1).singleOrNull { !it.isEmpty }?.location

        return if (target != null && startLocation.distanceBetween(target) > MY_LOCATION_MAX_RANGE) {
            withStops(listOf(RoutePlannerWaypoint()) + stops.drop(1))
        } else {
            this
        }
    }

    fun withStops(stops: List<RoutePlannerWaypoint>): RoutePlannerUiState =
        copy(waypoints = stops + (waypoints.lastOrNull() ?: RoutePlannerWaypoint()))

    companion object {
        const val MIN_WAYPOINT_COUNT = 2
        const val MAX_WAYPOINT_COUNT = 10

        private val MY_LOCATION_MAX_RANGE: Length = 25.kilometers

        private val DEFAULT_WAYPOINTS = listOf(
            RoutePlannerWaypoint(name = StringDesc.Resource(SharedRes.strings.route_planner_my_location)),
            RoutePlannerWaypoint(),
            RoutePlannerWaypoint(),
        )

        // Stable across states, so the location fix can find this stop when it arrives.
        val MY_LOCATION_WAYPOINT_ID = DEFAULT_WAYPOINTS.first().id

        val Default = RoutePlannerUiState()
    }
}
