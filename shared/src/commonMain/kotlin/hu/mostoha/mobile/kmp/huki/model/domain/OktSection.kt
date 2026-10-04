package hu.mostoha.mobile.kmp.huki.model.domain

import org.maplibre.spatialk.units.Length
import kotlin.time.Duration

/**
 * A section of a Blue Trail with the official, static stats.
 *
 * [stampRange] of neighbouring sections overlap, so a boundary stamping point belongs to both.
 */
data class OktSection(
    val id: String,
    val name: String,
    val distance: Length,
    val incline: Int,
    val decline: Int,
    val travelTime: Duration,
    val start: Location,
    val end: Location,
    val stampRange: ClosedFloatingPointRange<Double>,
)
