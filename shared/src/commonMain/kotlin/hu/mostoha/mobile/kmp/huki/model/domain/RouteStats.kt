package hu.mostoha.mobile.kmp.huki.model.domain

import org.maplibre.spatialk.units.Length
import kotlin.time.Duration

data class RouteStats(
    val travelTime: Duration,
    val distance: Length,
    val incline: Int,
    val decline: Int,
)
