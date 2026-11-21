package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox

object RoutePlanConstants {
    const val MIN_WAYPOINT_COUNT = 2
    const val MAX_WAYPOINT_COUNT = 15
    const val GRAPHHOPPER_URL = "https://www.graphhopper.com/"

    /**
     * Author of the GPX files saved by the Route Planner, which identifies them after they leave the app.
     */
    const val GPX_AUTHOR_NAME = "HuKi"

    /**
     * Collected route stops, keeping the planner's first stop free for my location.
     */
    const val MAX_ROUTE_STOPS = MAX_WAYPOINT_COUNT - 1

    /**
     * GraphHopper credits kept for routes abroad and for clients without the HuKi-Routing fallback.
     */
    const val GRAPHHOPPER_CREDIT_RESERVE = 500

    const val ROUND_TRIP_MIN_DISTANCE_KM = 1
    const val ROUND_TRIP_MAX_DISTANCE_KM = 30
    const val ROUND_TRIP_DEFAULT_DISTANCE_KM = 10

    /**
     * Seeds and start headings a new round trip is drawn from, each pair gives GraphHopper another loop.
     */
    val ROUND_TRIP_SEED_RANGE = 1..100
    val ROUND_TRIP_HEADING_RANGE = 0 until 360

    /**
     * Penalty in seconds for leaving the start in another direction than the drawn heading.
     */
    const val ROUND_TRIP_HEADING_PENALTY = 1000

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
