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
     * Geometry is counted, as the UiState is logged on every change and a long track makes the full string slow.
     */
    override fun toString(): String =
        "GpxDetails(layerId=$layerId, fileName=$fileName, locations=${locations.size}, " +
            "waypoints=${waypoints.size}, slopeSegments=${slopeSegments.size}, isReversed=$isReversed)"
}
