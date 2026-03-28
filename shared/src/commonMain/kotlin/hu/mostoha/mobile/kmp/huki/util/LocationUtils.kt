package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.turf.measurement.distance
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.meters
import kotlin.math.abs
import kotlin.math.min

fun Location.distanceBetween(other: Location): Length = distance(toPoint(), other.toPoint())

fun Location.toPoint(): Point =
    Point(
        longitude = this.longitude,
        latitude = this.latitude,
        altitude = this.altitude,
    )

/**
 * Calculates the center of the given [Location]s. It does not accurate for flat 180/-180 degrees.
 */
fun List<Location>.calculateCenter(): Location =
    Location(
        latitude = this.sumOf { it.latitude } / this.size,
        longitude = this.sumOf { it.longitude } / this.size,
    )

fun Location.isCloseWithThreshold(other: Location, threshold: Length = 20.meters): Boolean =
    this.distanceBetween(other) <= threshold

/**
 * Calculates the total distance between [Location]s returning a [Length].
 */
fun List<Location>.calculateTotalDistance(): Length {
    var distance = 0.meters
    forEachIndexed { index, location ->
        distance += location.distanceBetween(this[min(size - 1, index + 1)])
    }
    return distance
}

/**
 * Calculates the total incline of the given [Location]s.
 */
fun List<Location>.calculateIncline(): Int {
    val altitudes = mapNotNull { it.altitude?.toInt() }

    var totalIncline = 0

    altitudes.forEachIndexed { index, altitude ->
        val previousAltitude = altitudes.getOrNull(index - 1) ?: return@forEachIndexed
        val incline = if (previousAltitude < altitude) {
            altitude - previousAltitude
        } else {
            0
        }
        totalIncline += incline
    }

    return totalIncline
}

/**
 * Calculates the total decline of the given [Location]s.
 */
fun List<Location>.calculateDecline(): Int {
    val altitudes = mapNotNull { it.altitude?.toInt() }

    var totalDecline = 0

    altitudes.forEachIndexed { index, altitude ->
        val previousAltitude = altitudes.getOrNull(index - 1) ?: return@forEachIndexed
        val incline = if (previousAltitude > altitude) {
            abs(altitude - previousAltitude)
        } else {
            0
        }
        totalDecline += incline
    }

    return totalDecline
}
