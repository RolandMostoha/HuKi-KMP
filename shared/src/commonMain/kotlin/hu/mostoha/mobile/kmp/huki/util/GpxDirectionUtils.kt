package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import kotlin.uuid.Uuid

/**
 * Travel time, elevation stats and slopes are recalculated, as walking uphill is slower than downhill.
 */
fun GpxDetails.reversed(): GpxDetails {
    val reversedLocations = locations.reversed()
    return copy(
        layerId = Uuid.random().toString(),
        locations = reversedLocations,
        waypoints = waypoints.map { it.copy(type = it.type.reversed()) },
        travelTime = reversedLocations.calculateTravelTime(),
        incline = reversedLocations.calculateIncline(),
        decline = reversedLocations.calculateDecline(),
        slopeSegments = reversedLocations.calculateSlopeSegments(),
        isReversed = !isReversed,
    )
}

private fun WaypointType.reversed(): WaypointType =
    when (this) {
        WaypointType.START -> WaypointType.END
        WaypointType.END -> WaypointType.START
        WaypointType.INTERMEDIATE, WaypointType.ROUND_TRIP -> this
    }
