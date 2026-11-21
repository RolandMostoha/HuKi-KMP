package hu.mostoha.mobile.kmp.huki.features.routeplanner

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoundTripOptions
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlanRequest
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerMode
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerSession
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants
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
    val selectedStopId: String? = null,
    val renamingStopId: String? = null,
    val isMovingStop: Boolean = false,
    val myLocation: Location? = null,
    val mode: RoutePlannerMode = RoutePlannerMode.PLANNER,
    val roundTrip: RoundTripOptions? = null,
) {
    val isRoundTripWizard: Boolean
        get() = mode == RoutePlannerMode.ROUND_TRIP_WIZARD

    /**
     * The wizard's single stop, which its loop starts from and returns to.
     */
    val roundTripStart: RoutePlannerWaypoint?
        get() = stops.firstOrNull().takeIf { isRoundTripWizard }

    /**
     * Every loop costs a routing request, so a new one waits until the current one has arrived.
     */
    val isRegenerateEnabled: Boolean
        get() = isRoundTripWizard && roundTripStart?.location != null && !isRoutePlanLoading

    val stops: List<RoutePlannerWaypoint>
        get() = waypoints.dropLast(1)

    val routeStats: RouteStats?
        get() = routePlan?.routeStats

    val session: RoutePlannerSession
        get() = RoutePlannerSession(
            routeProfile = routeProfile,
            waypoints = waypoints,
            mode = mode,
            roundTrip = roundTrip,
        )

    val isRoundTrip: Boolean
        get() = stops.size >= RoutePlanConstants.MIN_WAYPOINT_COUNT &&
            stops.first().location != null &&
            stops.first().location == stops.last().location

    /**
     * A round trip's button undoes it, otherwise it closes the route back to its start.
     */
    val isRoundTripEnabled: Boolean
        get() = !isRoundTripWizard && (isRoundTrip || !isMaxStopsReached && stops.firstOrNull()?.location != null)

    val isMaxStopsReached: Boolean
        get() = stops.size >= RoutePlanConstants.MAX_WAYPOINT_COUNT

    /**
     * A selection whose stop was removed or emptied simply resolves to nothing.
     */
    val selectedStop: RoutePlannerWaypoint?
        get() = stops.firstOrNull { it.id == selectedStopId && !it.isEmpty }

    val renamingStop: RoutePlannerWaypoint?
        get() = stops.firstOrNull { it.id == renamingStopId && !it.isEmpty }

    /**
     * The filled stop the search result overwrites, null when the search fills an empty stop.
     */
    val replacedStop: RoutePlannerWaypoint?
        get() = stops.firstOrNull { it.id == waypointSearchTargetId && !it.isEmpty }

    /**
     * The first empty stop shows its actions unless another stop is selected, so only one stop is open at a time.
     */
    fun isStopExpanded(stop: RoutePlannerWaypoint): Boolean =
        if (stop.isEmpty) {
            selectedStop == null && stops.firstOrNull { it.isEmpty }?.id == stop.id
        } else {
            stop.id == selectedStop?.id
        }

    /**
     * Only one empty stop at a time, so the user fills it before asking for another. A round trip has to be undone
     * first, as its end mirrors the start.
     */
    val isAddStopEnabled: Boolean
        get() = !isRoundTripWizard && !isRoundTrip && !isMaxStopsReached && stops.none { it.isEmpty }

    val isEmptyStopRemovable: Boolean
        get() = stops.size > RoutePlanConstants.MIN_WAYPOINT_COUNT

    /**
     * The last stop links down only to the max stops row that follows it.
     */
    fun hasConnectorBelow(index: Int): Boolean = index < stops.lastIndex || isMaxStopsReached

    val isSaveEnabled: Boolean
        get() = routePlan != null && !isRoutePlanLoading

    val isRoutePlanExpected: Boolean
        get() = routePlanRequest.isRoutable

    /**
     * The stops can be routed and the plan is on its way, so the map waits for it before moving the camera.
     */
    val isAwaitingRoutePlan: Boolean
        get() = routePlan == null && routePlanError == null && isRoutePlanExpected

    val isStopListVisible: Boolean
        get() = routePlanError == null

    val isSaveButtonVisible: Boolean
        get() = routePlanError == null

    val routePlanMarkers: List<GpxWaypoint>
        get() = (if (isRoundTrip) stops.dropLast(1) else stops)
            .mapIndexedNotNull { index, stop ->
                stop.location?.let { location ->
                    GpxWaypoint(location, stopType(index))
                }
            }

    /**
     * The wizard routes a loop from its start alone.
     */
    val routePlanRequest: RoutePlanRequest
        get() = if (isRoundTripWizard) {
            RoutePlanRequest(
                routeProfile = routeProfile,
                waypoints = listOfNotNull(roundTripStart?.location),
                roundTrip = roundTrip,
            )
        } else {
            RoutePlanRequest(
                routeProfile = routeProfile,
                waypoints = stops.mapNotNull { it.location },
            )
        }

    fun stopType(index: Int): WaypointType =
        when {
            isRoundTripWizard -> WaypointType.ROUND_TRIP
            isRoundTrip && (index == 0 || index == stops.lastIndex) -> WaypointType.ROUND_TRIP
            index == 0 -> WaypointType.START
            index == stops.lastIndex -> WaypointType.END
            else -> WaypointType.INTERMEDIATE
        }

    fun withMyLocationRangeGuard(): RoutePlannerUiState {
        val startLocation = stops.firstOrNull()
            ?.takeIf { it.id == MY_LOCATION_WAYPOINT_ID }
            ?.location
            ?: return this
        val target = stops.drop(1).firstOrNull { !it.isEmpty }?.location

        if (target == null || startLocation.distanceBetween(target) <= MY_LOCATION_MAX_RANGE) return this
        val rest = stops.drop(1)
        // Enough stops left to form a route on their own, so no empty start is reserved
        return if (rest.count { !it.isEmpty } >= RoutePlanConstants.MIN_WAYPOINT_COUNT) {
            withStops(rest)
        } else {
            withStops(listOf(RoutePlannerWaypoint()) + rest)
        }
    }

    fun withStops(stops: List<RoutePlannerWaypoint>): RoutePlannerUiState =
        copy(waypoints = stops + (waypoints.lastOrNull() ?: RoutePlannerWaypoint()))

    companion object {
        private val MY_LOCATION_MAX_RANGE: Length = 25.kilometers

        /**
         * Stable across states and processes, so the location fix can find this stop, also after a restore.
         */
        const val MY_LOCATION_WAYPOINT_ID = "my_location"

        internal val MY_LOCATION_WAYPOINT = RoutePlannerWaypoint(
            id = MY_LOCATION_WAYPOINT_ID,
            name = StringDesc.Resource(SharedRes.strings.route_planner_my_location),
        )

        private val DEFAULT_WAYPOINTS = listOf(MY_LOCATION_WAYPOINT, RoutePlannerWaypoint(), RoutePlannerWaypoint())

        val Default = RoutePlannerUiState()
    }
}
