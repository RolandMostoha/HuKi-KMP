package hu.mostoha.mobile.kmp.huki.model.domain

import org.maplibre.spatialk.units.Length
import kotlin.time.Duration
import kotlin.time.Instant

data class GpxFileItem(
    val fileName: String,
    val fileUri: String,
    val trackId: String,
    val title: String?,
    val totalDistance: Length,
    val travelTime: Duration,
    val incline: Int,
    val decline: Int,
    val lastModified: Instant,
    val lastOpened: Instant?,
)
