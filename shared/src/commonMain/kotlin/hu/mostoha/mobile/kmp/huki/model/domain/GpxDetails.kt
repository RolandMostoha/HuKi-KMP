package hu.mostoha.mobile.kmp.huki.model.domain

import org.maplibre.spatialk.units.Length
import kotlin.time.Duration
import kotlin.uuid.Uuid

data class GpxDetails(
    val layerId: String = Uuid.random().toString(),
    val fileName: String,
    val fileUri: String,
    val title: String?,
    val locations: List<Location>,
    val waypoints: List<GpxWaypoint>,
    val bounds: List<Location>,
    val totalDistance: Length,
    val travelTime: Duration,
    val altitudeRange: Pair<Int, Int>,
    val incline: Int,
    val decline: Int,
    val jelSymbol: JelSymbol? = null,
    val slopeSegments: List<SlopeSegment> = emptyList(),
    val isReversed: Boolean = false,
    val isHukiAuthored: Boolean = false,
) {
    val isReversible: Boolean
        get() = locations.size >= 2

    val hasSlopeSegments: Boolean
        get() = slopeSegments.isNotEmpty()

    /**
     * The track's edge for the given direction, or the first / last waypoint of a waypoint-only GPX. `null` when
     * there is no distinct target, e.g. the end of a round trip or of a single-waypoint file.
     */
    fun mapsNavigationTarget(type: GpxMapsNavigationType): Location? {
        val edges = waypoints.filter { it.type != WaypointType.INTERMEDIATE }
        val target = if (edges.isEmpty()) {
            when (type) {
                GpxMapsNavigationType.START -> waypoints.firstOrNull()
                GpxMapsNavigationType.END -> waypoints.drop(1).lastOrNull()
            }
        } else {
            when (type) {
                GpxMapsNavigationType.START ->
                    edges.firstOrNull { it.type == WaypointType.START || it.type == WaypointType.ROUND_TRIP }
                GpxMapsNavigationType.END -> edges.firstOrNull { it.type == WaypointType.END }
            }
        }
        return target?.location
    }

    /**
     * Geometry is counted, as the UiState is logged on every change and a long track makes the full string slow.
     */
    override fun toString(): String =
        "GpxDetails(layerId=$layerId, fileName=$fileName, locations=${locations.size}, " +
            "waypoints=${waypoints.size}, slopeSegments=${slopeSegments.size}, isReversed=$isReversed)"
}
