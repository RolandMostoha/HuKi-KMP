package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RouteProgress
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters
import kotlin.math.PI
import kotlin.math.cos
import kotlin.time.Duration.Companion.hours

/**
 * Cumulative distances and Naismith travel times along a track, so the [RouteProgress] between two
 * [TrackPosition]s is a lookup instead of a walk over every vertex.
 *
 * Naismith is direction dependent, so the times are accumulated for both walking directions.
 */
class TrackProgressIndex(private val locations: List<Location>) {

    private val cumulativeMeters = DoubleArray(locations.size)
    private val forwardHours = DoubleArray(locations.size)
    private val backwardHours = DoubleArray(locations.size)

    init {
        for (index in 1 until locations.size) {
            val previous = locations[index - 1]
            val current = locations[index]
            val segmentKm = previous.distanceBetween(current).inMeters / METERS_IN_KM
            val inclineKm = ((current.altitude ?: 0.0) - (previous.altitude ?: 0.0)) / METERS_IN_KM
            cumulativeMeters[index] = cumulativeMeters[index - 1] + segmentKm * METERS_IN_KM
            forwardHours[index] = forwardHours[index - 1] + naismith(segmentKm, inclineKm)
            backwardHours[index] = backwardHours[index - 1] + naismith(segmentKm, -inclineKm)
        }
    }

    fun positionOf(location: Location): TrackPosition {
        if (locations.size < 2) {
            return TrackPosition(segmentIndex = 0, fraction = 0.0, offTrackDistance = 0.meters)
        }
        // Equirectangular projection: accurate enough to pick the nearest segment at hiking scale
        val longitudeScale = cos(location.latitude * PI / 180)
        var nearestIndex = 0
        var nearestFraction = 0.0
        var nearestDistance = Double.MAX_VALUE
        for (index in 0 until locations.size - 1) {
            val start = locations[index]
            val end = locations[index + 1]
            val segmentX = (end.longitude - start.longitude) * longitudeScale
            val segmentY = end.latitude - start.latitude
            val pointX = (location.longitude - start.longitude) * longitudeScale
            val pointY = location.latitude - start.latitude
            val segmentLengthSquared = segmentX * segmentX + segmentY * segmentY
            val fraction = if (segmentLengthSquared == 0.0) {
                0.0
            } else {
                ((pointX * segmentX + pointY * segmentY) / segmentLengthSquared).coerceIn(0.0, 1.0)
            }
            val dx = pointX - segmentX * fraction
            val dy = pointY - segmentY * fraction
            val distance = dx * dx + dy * dy
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestIndex = index
                nearestFraction = fraction
            }
        }
        val start = locations[nearestIndex]
        val end = locations[nearestIndex + 1]
        val snapped = Location(
            latitude = start.latitude + (end.latitude - start.latitude) * nearestFraction,
            longitude = start.longitude + (end.longitude - start.longitude) * nearestFraction,
        )
        return TrackPosition(
            segmentIndex = nearestIndex,
            fraction = nearestFraction,
            offTrackDistance = location.distanceBetween(snapped),
        )
    }

    /**
     * Walks towards [to] in whichever direction it lies along the track.
     */
    fun progress(from: TrackPosition, to: TrackPosition): RouteProgress {
        val fromMeters = cumulativeMeters.at(from)
        val toMeters = cumulativeMeters.at(to)
        val hours = if (toMeters >= fromMeters) {
            forwardHours.at(to) - forwardHours.at(from)
        } else {
            backwardHours.at(from) - backwardHours.at(to)
        }
        return RouteProgress(
            distance = (toMeters - fromMeters).let { if (it < 0) -it else it }.meters,
            travelTime = hours.hours,
        )
    }

    /**
     * Both distance and Naismith time are linear in the walked part of a single segment.
     */
    private fun DoubleArray.at(position: TrackPosition): Double {
        val start = this[position.segmentIndex]
        val end = getOrElse(position.segmentIndex + 1) { start }
        return start + (end - start) * position.fraction
    }

    private companion object {
        const val METERS_IN_KM = 1000.0
    }
}

data class TrackPosition(
    val segmentIndex: Int,
    val fraction: Double,
    val offTrackDistance: Length,
)
