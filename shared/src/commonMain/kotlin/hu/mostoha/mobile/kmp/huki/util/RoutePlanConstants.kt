package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox

object RoutePlanConstants {
    const val MIN_WAYPOINT_COUNT = 2
    const val MAX_WAYPOINT_COUNT = 10

    /**
     * Collected route stops, keeping the planner's first stop free for my location.
     */
    const val MAX_ROUTE_STOPS = MAX_WAYPOINT_COUNT - 1

    /**
     * GraphHopper credits kept for routes abroad and for clients without the HuKi-Routing fallback.
     */
    const val GRAPHHOPPER_CREDIT_RESERVE = 500

    /**
     * Graph bounds of HuKi-Routing, it fails fast with HTTP 400 for waypoints without road data inside.
     */
    val HUKI_ROUTING_BOUNDING_BOX = BoundingBox(
        north = 48.60,
        east = 22.94,
        south = 45.71,
        west = 16.09,
    )
}
