package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants

data class RoutePlanRequest(
    val routeProfile: RoutePlannerProfile,
    val waypoints: List<Location>,
    val roundTrip: RoundTripOptions? = null,
) {
    /**
     * A round trip is routed from its start alone, any other route needs at least two stops.
     */
    val isRoutable: Boolean
        get() = if (roundTrip != null) {
            waypoints.size == 1
        } else {
            waypoints.size >= RoutePlanConstants.MIN_WAYPOINT_COUNT
        }
}
