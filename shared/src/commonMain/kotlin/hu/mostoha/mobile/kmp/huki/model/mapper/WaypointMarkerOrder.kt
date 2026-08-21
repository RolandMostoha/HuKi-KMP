package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType

object WaypointMarkerOrder {

    private val drawOrder = listOf(
        WaypointType.INTERMEDIATE,
        WaypointType.START,
        WaypointType.END,
        WaypointType.ROUND_TRIP,
    )

    fun sort(waypoints: List<GpxWaypoint>): List<GpxWaypoint> = waypoints.sortedBy { drawOrder.indexOf(it.type) }
}
