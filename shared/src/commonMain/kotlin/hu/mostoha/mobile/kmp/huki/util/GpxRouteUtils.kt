package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RouteProgress
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.turf.measurement.length
import org.maplibre.spatialk.turf.misc.nearestPointTo
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.Duration.Companion.ZERO

/**
 * Calculates [RouteProgress] from [from] (e.g. the user's location) to [to] (a waypoint), following this track.
 *
 * Both points are snapped onto the track; the along-track distance is the difference of their snapped
 * positions. For a round trip the distance is measured forward (wrapping past the start/end).
 */
fun List<Location>.routeProgressTo(from: Location, to: Location, isRoundTrip: Boolean): RouteProgress {
    if (size < 2) {
        return RouteProgress(distance = from.distanceBetween(to), travelTime = ZERO)
    }

    val line = LineString(map { it.toPoint().coordinates })
    val totalLength = line.length()

    val fromAlong = line.nearestPointTo(from.toPoint().coordinates).properties.location
    val toAlong = line.nearestPointTo(to.toPoint().coordinates).properties.location

    val alongTrack = if (isRoundTrip && toAlong < fromAlong) {
        (totalLength - fromAlong) + toAlong
    } else {
        (toAlong - fromAlong).absoluteValue()
    }
    val walked = walkedSubTrack(fromAlong, toAlong, totalLength, isRoundTrip)

    return RouteProgress(
        distance = alongTrack,
        travelTime = walked.calculateTravelTime(),
    )
}

/**
 * Ordered locations walked from [fromAlong] to [toAlong], preserving direction (and wrapping for a round trip),
 * so the elevation profile stays correct for the Naismith travel time.
 */
private fun List<Location>.walkedSubTrack(
    fromAlong: Length,
    toAlong: Length,
    totalLength: Length,
    isRoundTrip: Boolean,
): List<Location> {
    val cumulative = cumulativeLengths()
    val from = fromAlong.inMeters
    val to = toAlong.inMeters

    val alongs = when {
        isRoundTrip && to < from ->
            listOf(from) + verticesBetween(cumulative, from, totalLength.inMeters) +
                totalLength.inMeters + 0.0 + verticesBetween(cumulative, 0.0, to) + to
        to >= from -> listOf(from) + verticesBetween(cumulative, from, to) + to
        else -> (listOf(to) + verticesBetween(cumulative, to, from) + from).reversed()
    }

    return alongs.map { interpolateAt(cumulative, it) }
}

private fun List<Location>.cumulativeLengths(): DoubleArray {
    val cumulative = DoubleArray(size)
    for (index in 1 until size) {
        cumulative[index] = cumulative[index - 1] + this[index - 1].distanceBetween(this[index]).inMeters
    }
    return cumulative
}

private fun verticesBetween(cumulative: DoubleArray, startMeters: Double, endMeters: Double): List<Double> =
    cumulative.filter { it > startMeters && it < endMeters }

private fun List<Location>.interpolateAt(cumulative: DoubleArray, alongMeters: Double): Location {
    val clamped = alongMeters.coerceIn(0.0, cumulative.last())
    val upper = cumulative.indexOfFirst { it >= clamped }.coerceAtLeast(1)
    val lower = upper - 1
    val segment = cumulative[upper] - cumulative[lower]
    val fraction = if (segment == 0.0) 0.0 else (clamped - cumulative[lower]) / segment

    val start = this[lower]
    val end = this[upper]
    return Location(
        latitude = start.latitude + (end.latitude - start.latitude) * fraction,
        longitude = start.longitude + (end.longitude - start.longitude) * fraction,
        altitude = lerpAltitude(start.altitude, end.altitude, fraction),
    )
}

private fun lerpAltitude(start: Double?, end: Double?, fraction: Double): Double? {
    if (start == null || end == null) return start ?: end
    return start + (end - start) * fraction
}

private fun Length.absoluteValue(): Length = if (this < 0.meters) -this else this
