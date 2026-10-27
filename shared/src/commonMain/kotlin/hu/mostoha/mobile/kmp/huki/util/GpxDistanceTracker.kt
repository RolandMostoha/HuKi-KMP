package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.toDistanceInfoWindow
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters

/**
 * Measures waypoint distances along a GPX track from the user, or from the start when the user is unknown or off
 * the route. Passed and nearby waypoints are left out, unless selected.
 */
class GpxDistanceTracker(
    locations: List<Location>,
    private val waypoints: List<GpxWaypoint>,
) {

    private val track = TrackProgressIndex(locations)
    private val startPosition = TrackPosition(segmentIndex = 0, fraction = 0.0, offTrackDistance = 0.meters)
    private val endAlong = track.distanceAlong(track.endPosition)
    private val isRoundTrip = waypoints.any { it.type == WaypointType.ROUND_TRIP }

    // A round trip finishes where it starts, so its marker stands for the end of the loop
    private val waypointPositions = waypoints.map { waypoint ->
        if (waypoint.type == WaypointType.ROUND_TRIP) track.endPosition else track.positionOf(waypoint.location)
    }

    private var lastUserAlong: Length? = null

    fun distanceInfoWindows(
        userLocation: Location?,
        showAll: Boolean,
        selected: GpxWaypoint?,
    ): List<DistanceInfoWindowData> {
        if (!showAll && selected == null) return emptyList()

        val userPosition = userLocation?.let { userPositionOf(it) }
        val from = userPosition ?: startPosition
        val fromAlong = track.distanceAlong(from)

        return waypoints.mapIndexedNotNull { index, waypoint ->
            val isSelected = waypoint == selected
            val to = waypointPositions[index]
            val ahead = track.distanceAlong(to) - fromAlong
            val isStart = waypoint.type == WaypointType.START
            val isHidden = when {
                isSelected -> false
                !showAll -> true
                isStart -> userPosition != null && ahead < -DistanceInfoWindowConfig.NEAR_DISTANCE
                else -> ahead < DistanceInfoWindowConfig.NEAR_DISTANCE
            }
            when {
                isHidden -> null
                isStart -> waypoint.toDistanceInfoWindow(id = index, progress = null)
                else -> waypoint.toDistanceInfoWindow(id = index, progress = track.progress(from, to))
            }
        }
    }

    private fun userPositionOf(location: Location): TrackPosition? {
        val position = track.positionOf(location)
            .takeIf { it.offTrackDistance <= DistanceInfoWindowConfig.OFF_ROUTE_DISTANCE }
            ?: return null
        val along = track.distanceAlong(position)
        val previousAlong = lastUserAlong
        // On a loop the shared start/finish snaps to the start, so arriving there from the second half is finishing
        val isFinishingLoop = isRoundTrip &&
            previousAlong != null &&
            previousAlong.inMeters > endAlong.inMeters / 2 &&
            along < DistanceInfoWindowConfig.NEAR_DISTANCE
        val adjusted = if (isFinishingLoop) track.endPosition else position
        lastUserAlong = track.distanceAlong(adjusted)
        return adjusted
    }
}
