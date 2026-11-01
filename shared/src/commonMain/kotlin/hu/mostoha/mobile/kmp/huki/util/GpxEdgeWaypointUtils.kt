package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType

/**
 * Names the edge markers after the named waypoints lying exactly on the track's first or last point, which are then
 * dropped so they aren't drawn twice. A nearby waypoint (e.g. an OKT stamp point) is a different place, so it stays.
 */
fun List<GpxWaypoint>.withNamedEdges(edges: List<GpxWaypoint>, locations: List<Location>): List<GpxWaypoint> {
    val consumedIndices = mutableSetOf<Int>()
    val namedEdges = edges.map { edge ->
        val edgeLocations = when (edge.type) {
            WaypointType.ROUND_TRIP -> listOfNotNull(locations.firstOrNull(), locations.lastOrNull())
            WaypointType.START, WaypointType.END, WaypointType.INTERMEDIATE -> listOf(edge.location)
        }
        val matchIndices = indices.filter { index ->
            val waypoint = get(index)
            !waypoint.name.isNullOrBlank() && edgeLocations.any { it.hasSameCoordinates(waypoint.location) }
        }
        consumedIndices += matchIndices
        matchIndices.firstOrNull()
            ?.let { get(it) }
            ?.let { match -> edge.copy(name = match.name, description = match.description) }
            ?: edge
    }
    return filterIndexed { index, _ -> index !in consumedIndices } + namedEdges
}

private fun Location.hasSameCoordinates(other: Location): Boolean =
    latitude == other.latitude && longitude == other.longitude
