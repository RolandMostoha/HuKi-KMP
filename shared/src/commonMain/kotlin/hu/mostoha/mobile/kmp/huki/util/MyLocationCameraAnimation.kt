package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Cap of the camera transition to my location, which Mapbox's default transition shortens to but never stretches.
 *
 * Grows by [DURATION_PER_STEP] per step: one per zoom level, plus one per doubling of the on-screen distance.
 */
object MyLocationCameraAnimation {

    val MIN_DURATION: Duration = 500.milliseconds
    val MAX_DURATION: Duration = 2000.milliseconds
    val DURATION_PER_STEP: Duration = 200.milliseconds

    /**
     * On-screen distance in points (dp / pt) that counts as the first distance step.
     */
    const val SCREEN_DISTANCE_PER_STEP = 400.0

    fun maxDuration(camera: CameraPosition, myLocation: Location?, alreadyFollowing: Boolean): Duration {
        val steps = zoomSteps(camera, alreadyFollowing) + distanceSteps(camera, myLocation)
        return (MIN_DURATION + DURATION_PER_STEP * steps).coerceAtMost(MAX_DURATION)
    }

    private fun zoomSteps(camera: CameraPosition, alreadyFollowing: Boolean): Double =
        if (alreadyFollowing) 0.0 else abs(maxOf(camera.zoom, MapConstants.FOLLOW_LOCATION_ZOOM_LEVEL) - camera.zoom)

    private fun distanceSteps(camera: CameraPosition, myLocation: Location?): Double {
        myLocation ?: return 0.0
        val screenDistance = camera.location.distanceBetween(myLocation).inMeters / camera.metersPerPoint()
        return log2(1 + screenDistance / SCREEN_DISTANCE_PER_STEP)
    }

    /**
     * Web Mercator ground resolution at the camera center, for Mapbox's 512 point tiles.
     */
    private fun CameraPosition.metersPerPoint(): Double =
        EARTH_CIRCUMFERENCE_METERS * cos(location.latitude * PI / 180) / (MAPBOX_TILE_SIZE * 2.0.pow(zoom))

    private const val EARTH_CIRCUMFERENCE_METERS = 40_075_016.686
    private const val MAPBOX_TILE_SIZE = 512
}
