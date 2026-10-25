package hu.mostoha.mobile.kmp.huki.util

import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.kilometers
import org.maplibre.spatialk.units.extensions.meters

object DistanceInfoWindowConfig {
    /**
     * Waypoints closer than this along the track carry no useful distance, so their windows are hidden.
     */
    val NEAR_DISTANCE: Length = 50.meters

    /**
     * Farther than this from the track the user isn't hiking it, so distances are measured from the start.
     */
    val OFF_ROUTE_DISTANCE: Length = 1.kilometers

    /**
     * Smaller location changes are GPS jitter, so the windows aren't recalculated for them.
     */
    val MIN_USER_MOVE: Length = 15.meters
}
