package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.MyLocationCameraAnimation.DURATION_PER_STEP
import hu.mostoha.mobile.kmp.huki.util.MyLocationCameraAnimation.MAX_DURATION
import hu.mostoha.mobile.kmp.huki.util.MyLocationCameraAnimation.MIN_DURATION
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Camera transition to a route overview, timed like [MyLocationCameraAnimation]. A very far route is jumped close to,
 * as a long flight streams tiles of every zoom level on the way and stutters.
 */
object OverviewCameraAnimation {

    /**
     * Distance steps (see [MyLocationCameraAnimation.SCREEN_DISTANCE_PER_STEP]) beyond which the flight gets
     * [FAR_EXTRA_DURATION].
     */
    const val FAR_DISTANCE_STEPS = 3.0

    /**
     * Distance steps beyond which the camera jumps close to the route and arrives from there.
     */
    const val VERY_FAR_DISTANCE_STEPS = 6.0

    val FAR_EXTRA_DURATION: Duration = 600.milliseconds
    val FAR_MAX_DURATION: Duration = 3000.milliseconds

    /**
     * Zoom levels below the overview the jump lands on, so the arrival zooms in.
     */
    const val JUMP_ZOOM_OUT = 1.5

    /**
     * On-screen distance in points (dp / pt) at the jump's zoom, back towards where the camera came from.
     */
    const val JUMP_OFFSET_POINTS = 300.0

    /**
     * Lets the map apply the jump before the arrival starts, which would otherwise cancel it.
     */
    val JUMP_SETTLE_DELAY: Duration = 100.milliseconds

    /**
     * On-screen size in points (dp / pt) the route roughly fills, to estimate the overview zoom.
     */
    const val OVERVIEW_SIZE_POINTS = 300.0

    fun transition(camera: CameraPosition, bounds: List<Location>): OverviewCameraTransition {
        if (bounds.isEmpty()) return OverviewCameraTransition.Animate(MIN_DURATION)
        val center = bounds.boundingBoxCenter()
        val overviewZoom = overviewZoom(bounds, center, camera.zoom)
        val distanceSteps = MyLocationCameraAnimation.distanceSteps(camera, center)
        return when {
            distanceSteps > VERY_FAR_DISTANCE_STEPS -> {
                val start = jumpStart(camera.location, center, overviewZoom)
                OverviewCameraTransition.JumpAndArrive(start, duration(start, center, overviewZoom, MAX_DURATION))
            }
            distanceSteps > FAR_DISTANCE_STEPS -> OverviewCameraTransition.Animate(
                (duration(camera, center, overviewZoom, FAR_MAX_DURATION) + FAR_EXTRA_DURATION)
                    .coerceAtMost(FAR_MAX_DURATION),
            )
            else -> OverviewCameraTransition.Animate(duration(camera, center, overviewZoom, MAX_DURATION))
        }
    }

    private fun duration(
        camera: CameraPosition,
        center: Location,
        overviewZoom: Double,
        max: Duration,
    ): Duration {
        val steps = abs(overviewZoom - camera.zoom) + MyLocationCameraAnimation.distanceSteps(camera, center)
        return (MIN_DURATION + DURATION_PER_STEP * steps).coerceAtMost(max)
    }

    private fun jumpStart(origin: Location, center: Location, overviewZoom: Double): CameraPosition {
        val zoom = overviewZoom - JUMP_ZOOM_OUT
        val offsetMeters = JUMP_OFFSET_POINTS * MyLocationCameraAnimation.metersPerPoint(center, zoom)
        // Equirectangular direction from the route back to the origin, precise enough to pick a side
        val east = (origin.longitude - center.longitude) * cos(center.latitude * DEGREES_TO_RADIANS)
        val north = origin.latitude - center.latitude
        val length = hypot(east, north)
        val location = if (length == 0.0) {
            center
        } else {
            Location(
                latitude = center.latitude + north / length * offsetMeters / METERS_PER_DEGREE,
                longitude = center.longitude +
                    east / length * offsetMeters / (METERS_PER_DEGREE * cos(center.latitude * DEGREES_TO_RADIANS)),
            )
        }
        return CameraPosition(location, zoom, bearing = 0.0, pitch = 0.0)
    }

    private fun overviewZoom(bounds: List<Location>, center: Location, fallbackZoom: Double): Double {
        val minLatitude = bounds.minOf { it.latitude }
        val maxLatitude = bounds.maxOf { it.latitude }
        val minLongitude = bounds.minOf { it.longitude }
        val maxLongitude = bounds.maxOf { it.longitude }
        val width = Location(center.latitude, minLongitude).distanceBetween(Location(center.latitude, maxLongitude))
        val height = Location(minLatitude, center.longitude).distanceBetween(Location(maxLatitude, center.longitude))
        val span = maxOf(width.inMeters, height.inMeters)
        if (span <= 0.0) return fallbackZoom
        return MyLocationCameraAnimation.zoomFor(center, metersPerPoint = span / OVERVIEW_SIZE_POINTS)
    }

    private fun List<Location>.boundingBoxCenter(): Location =
        Location(
            latitude = (minOf { it.latitude } + maxOf { it.latitude }) / 2,
            longitude = (minOf { it.longitude } + maxOf { it.longitude }) / 2,
        )

    private const val DEGREES_TO_RADIANS = kotlin.math.PI / 180
    private const val METERS_PER_DEGREE = 111_320.0
}

sealed interface OverviewCameraTransition {

    data class Animate(val duration: Duration) : OverviewCameraTransition

    /**
     * Jumps to [start] without animation, then animates to the overview over [duration].
     */
    data class JumpAndArrive(
        val start: CameraPosition,
        val duration: Duration,
    ) : OverviewCameraTransition
}
