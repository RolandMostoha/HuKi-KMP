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

    fun sort(waypoints: List<GpxWaypoint>): List<GpxWaypoint> = waypoints.sortedBy { sortKey(it.type) }

    /**
     * Mapbox draws lower symbol sort keys first, so higher keys end up on top.
     */
    fun sortKey(type: WaypointType): Double = drawOrder.indexOf(type).toDouble()
}
