package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location

object MapConstants {
    const val HUNGARY_ZOOM_LEVEL = 5.4
    const val HUNGARY_CENTER_LATITUDE = 47.162494
    const val HUNGARY_CENTER_LONGITUDE = 19.503304
    val HUNGARY_LOCATION = Location(HUNGARY_CENTER_LATITUDE, HUNGARY_CENTER_LONGITUDE)
    val HUNGARY_CAMERA_POSITION = CameraPosition(
        zoom = HUNGARY_ZOOM_LEVEL,
        location = HUNGARY_LOCATION,
        bearing = 0.0,
        pitch = 0.0,
    )

    const val NEARBY_DESTINATIONS_MIN_ZOOM = 6.0

    const val PLACE_DEFAULT_CAMERA_ZOOM = 16.0
    const val FOLLOW_LOCATION_ZOOM_LEVEL = 16.0
    const val FOLLOW_LOCATION_LIVE_COMPASS_PITCH = 45.0
    const val MAP_ROTATION_ENABLED = true

    /**
     * Smallest bearing change worth reacting to, matching Mapbox's own compass throttling.
     */
    const val MAP_BEARING_EPSILON = 0.1

    /**
     * Bearing tolerance in degrees within which the camera counts as facing north, for compass uses to fade out.
     */
    const val MAP_FACING_NORTH_TOLERANCE = 1.0

    const val MAP_ZOOM_STEP = 1.0
    const val MAP_MIN_ZOOM = 3.0
    const val MAP_MAX_ZOOM = 20.0
}
