package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

object MapConfiguration {
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

    const val FOLLOW_LOCATION_ZOOM_LEVEL = 16.0
    const val FOLLOW_LOCATION_PITCH = 45.0
    const val MAP_ROTATION_ENABLED = false

    val MAP_CAMERA_ANIM_DURATION: Duration = 800.milliseconds
    val MAP_CAMERA_ANIM_DURATION_S = MAP_CAMERA_ANIM_DURATION.toDouble(DurationUnit.SECONDS)
    val MAP_FOLLOW_ANIM_DURATION: Duration = 500.milliseconds
    val MAP_FOLLOW_ANIM_DURATION_S = MAP_FOLLOW_ANIM_DURATION.toDouble(DurationUnit.SECONDS)
}
