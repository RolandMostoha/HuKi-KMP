package hu.mostoha.mobile.kmp.huki.model.domain

import org.maplibre.spatialk.units.Length
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
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
)

fun GpxDetails.toGpxFileItem(trackId: String, lastModified: Instant, lastOpened: Instant?): GpxFileItem =
    GpxFileItem(
        fileName = fileName,
        fileUri = fileUri,
        trackId = trackId,
        title = title,
        totalDistance = totalDistance,
        travelTime = travelTime,
        incline = incline,
        decline = decline,
        lastModified = lastModified,
        lastOpened = lastOpened,
    )
