package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.SlopeSegment
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

object SlopeConstants {
    /**
     * Absorbs GPS / DEM noise on flat sections.
     */
    const val FLAT_SLOPE_PERCENT = 3.0
    const val MID_SLOPE_PERCENT = 10.0
    const val HIGH_SLOPE_PERCENT = 25.0

    /**
     * Minimum distance a slope is measured over, as point-to-point slopes amplify GPS / DEM altitude noise.
     */
    const val WINDOW_METERS = 100.0
    const val SEGMENT_LENGTH_METERS = 20.0

    /**
     * Caps the map features of long tracks, which get longer segments instead.
     */
    const val MAX_SEGMENTS = 500
}

private const val PERCENTAGE_SCALAR = 100

/**
 * Splits the track in its current direction into even segments carrying their slope. Neighbouring flat segments
 * are merged, as they share a color. Returns an empty list for tracks without altitude data.
 */
fun List<Location>.calculateSlopeSegments(): List<SlopeSegment> {
    val trackDistances = trackDistances()
    val totalDistance = trackDistances.lastOrNull() ?: 0.0
    val altitudeProfile = indices.mapNotNull { index -> this[index].altitude?.let { trackDistances[index] to it } }
    if (altitudeProfile.size < 2 || totalDistance <= 0.0) return emptyList()

    val segmentCount = ceil(totalDistance / SlopeConstants.SEGMENT_LENGTH_METERS).toInt()
        .coerceIn(1, SlopeConstants.MAX_SEGMENTS)
    val segmentLength = totalDistance / segmentCount
    val window = min(max(SlopeConstants.WINDOW_METERS, segmentLength), totalDistance)

    val ranges = (0 until segmentCount).map { index ->
        val start = index * segmentLength
        val end = if (index == segmentCount - 1) totalDistance else start + segmentLength
        val windowStart = ((start + end - window) / 2).coerceIn(0.0, totalDistance - window)
        val altitudeChange = altitudeProfile.altitudeAt(windowStart + window) - altitudeProfile.altitudeAt(windowStart)
        SlopeRange(start, end, altitudeChange / window * PERCENTAGE_SCALAR)
    }

    val mergedRanges = ranges.mergeFlat()
    // Shared by neighbouring segments, so they join without a gap
    val boundaries = (mergedRanges.map { it.start } + totalDistance).map { distance ->
        locationAt(trackDistances, trackDistances.lastIndexAtMost(distance), distance)
    }

    return mergedRanges.mapIndexed { index, range ->
        val interior = trackDistances.interiorIndices(range.start, range.end)
        SlopeSegment(
            locations = listOf(boundaries[index]) + interior.map { this[it] } + boundaries[index + 1],
            slopePercent = range.slopePercent,
        )
    }
}

private data class SlopeRange(
    val start: Double,
    val end: Double,
    val slopePercent: Double,
) {
    val isFlat: Boolean
        get() = abs(slopePercent) <= SlopeConstants.FLAT_SLOPE_PERCENT
}

private fun List<SlopeRange>.mergeFlat(): List<SlopeRange> {
    val merged = mutableListOf<SlopeRange>()
    forEach { range ->
        val previous = merged.lastOrNull()
        if (previous != null && previous.isFlat && range.isFlat) {
            merged[merged.lastIndex] = previous.copy(end = range.end)
        } else {
            merged.add(range)
        }
    }
    return merged
}

private fun List<Location>.trackDistances(): DoubleArray {
    val trackDistances = DoubleArray(size)
    for (index in 1 until size) {
        trackDistances[index] = trackDistances[index - 1] + this[index - 1].distanceBetween(this[index]).inMeters
    }
    return trackDistances
}

/**
 * Index of the last location at or before [distance], never the last one, so it always has a next location.
 */
private fun DoubleArray.lastIndexAtMost(distance: Double): Int =
    (firstIndexAbove(distance, inclusive = false) - 1).coerceIn(0, size - 2)

/**
 * Indices of the locations strictly between two track distances.
 */
private fun DoubleArray.interiorIndices(start: Double, end: Double): IntRange =
    firstIndexAbove(start, inclusive = false) until firstIndexAbove(end, inclusive = true)

/**
 * Index of the first distance above [value] (or equal to it when [inclusive]), or the size when there is none.
 */
private fun DoubleArray.firstIndexAbove(value: Double, inclusive: Boolean): Int {
    var low = 0
    var high = size
    while (low < high) {
        val middle = (low + high) / 2
        val isBefore = if (inclusive) this[middle] < value else this[middle] <= value
        if (isBefore) low = middle + 1 else high = middle
    }
    return low
}

/**
 * Interpolates the location at [distance], which lies between the locations at [index] and the next one.
 */
private fun List<Location>.locationAt(trackDistances: DoubleArray, index: Int, distance: Double): Location {
    val from = this[index]
    val to = getOrNull(index + 1) ?: return from
    val length = trackDistances[index + 1] - trackDistances[index]
    if (length <= 0.0) return from
    val fraction = ((distance - trackDistances[index]) / length).coerceIn(0.0, 1.0)
    if (fraction == 1.0) return to

    return Location(
        latitude = from.latitude + (to.latitude - from.latitude) * fraction,
        longitude = from.longitude + (to.longitude - from.longitude) * fraction,
        altitude = if (from.altitude != null && to.altitude != null) {
            from.altitude + (to.altitude - from.altitude) * fraction
        } else {
            from.altitude
        },
    )
}

/**
 * Linear interpolation over a profile of `(trackDistance, altitude)` pairs, sorted by distance.
 */
private fun List<Pair<Double, Double>>.altitudeAt(distance: Double): Double {
    val upperIndex = binarySearch { (pointDistance, _) -> pointDistance.compareTo(distance) }
        .let { if (it < 0) -(it + 1) else it }
    if (upperIndex == 0) return first().second
    if (upperIndex == size) return last().second

    val (lowerDistance, lowerAltitude) = this[upperIndex - 1]
    val (upperDistance, upperAltitude) = this[upperIndex]
    if (upperDistance == lowerDistance) return upperAltitude

    return lowerAltitude + (upperAltitude - lowerAltitude) * (distance - lowerDistance) / (upperDistance - lowerDistance)
}
