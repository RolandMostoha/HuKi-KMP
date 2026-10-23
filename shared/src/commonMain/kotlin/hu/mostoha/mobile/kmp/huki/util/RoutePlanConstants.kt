package hu.mostoha.mobile.kmp.huki.util

object RoutePlanConstants {
    const val MIN_WAYPOINT_COUNT = 2
    const val MAX_WAYPOINT_COUNT = 10

    /**
     * Collected route stops, keeping the planner's first stop free for my location.
     */
    const val MAX_ROUTE_STOPS = MAX_WAYPOINT_COUNT - 1
}
