package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.turf.measurement.distance
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
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

/**
 * Index of the closest location, compared on an equirectangular projection which is precise enough at hiking
 * scale and avoids a haversine per point on long tracks.
 */
fun List<Location>.nearestIndexTo(target: Location): Int {
    require(isNotEmpty()) { "Cannot find the nearest location of an empty list." }

    val longitudeScale = cos(target.latitude * PI / 180)
    var nearestIndex = 0
    var nearestDistance = Double.MAX_VALUE
    forEachIndexed { index, location ->
        val dx = (location.longitude - target.longitude) * longitudeScale
        val dy = location.latitude - target.latitude
        val distance = dx * dx + dy * dy
        if (distance < nearestDistance) {
            nearestDistance = distance
            nearestIndex = index
        }
    }
    return nearestIndex
}

/**
 * Along-track distance between the consecutive [points] snapped onto this track, the first one measured from the
 * track start. A point snapped behind its predecessor yields zero.
 */
fun List<Location>.legDistancesTo(points: List<Location>): List<Length> {
    if (isEmpty()) return points.map { 0.meters }

    val trackDistances = DoubleArray(size)
    for (index in 1 until size) {
        trackDistances[index] = trackDistances[index - 1] + this[index - 1].distanceBetween(this[index]).inMeters
    }
    val pointTrackDistances = points.map { trackDistances[nearestIndexTo(it)] }

    return pointTrackDistances.mapIndexed { index, distance ->
        val previousDistance = pointTrackDistances.getOrElse(index - 1) { 0.0 }
        (distance - previousDistance).coerceAtLeast(0.0).meters
    }
}

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
