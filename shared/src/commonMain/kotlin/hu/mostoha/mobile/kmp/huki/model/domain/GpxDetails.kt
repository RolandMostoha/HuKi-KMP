package hu.mostoha.mobile.kmp.huki.model.domain

import org.maplibre.spatialk.units.Length
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class GpxDetails(
    val layerId: String = Uuid.random().toString(),
    val fileName: String,
    val fileUri: String,
    val locations: List<Location>,
    val waypoints: List<GpxWaypoint>,
    val totalDistance: Length,
    val travelTime: Duration,
    val altitudeRange: Pair<Int, Int>,
    val incline: Int,
    val decline: Int,
)
