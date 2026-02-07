package hu.mostoha.mobile.kmp.huki.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

object MapConfiguration {
    const val HUNGARY_ZOOM_LEVEL = 5.4
    const val HUNGARY_CENTER_LATITUDE = 47.162494
    const val HUNGARY_CENTER_LONGITUDE = 19.503304

    const val FOLLOW_LOCATION_ZOOM_LEVEL = 16.0
    const val FOLLOW_LOCATION_PITCH = 45.0
    const val MAP_ROTATION_ENABLED = false

    val MAP_CAMERA_ANIM_DURATION: Duration = 800.milliseconds
    val MAP_CAMERA_ANIM_DURATION_S = MAP_CAMERA_ANIM_DURATION.toDouble(DurationUnit.SECONDS)
    val MAP_FOLLOW_ANIM_DURATION: Duration = 500.milliseconds
    val MAP_FOLLOW_ANIM_DURATION_S = MAP_FOLLOW_ANIM_DURATION.toDouble(DurationUnit.SECONDS)
}
