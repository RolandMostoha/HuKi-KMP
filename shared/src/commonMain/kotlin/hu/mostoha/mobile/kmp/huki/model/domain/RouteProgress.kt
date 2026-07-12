package hu.mostoha.mobile.kmp.huki.model.domain

import org.maplibre.spatialk.units.Length
import kotlin.time.Duration

data class RouteProgress(
    val distance: Length,
    val travelTime: Duration,
)
