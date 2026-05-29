package hu.mostoha.mobile.kmp.huki.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

object AnimationConstants {
    val MAP_CAMERA_ANIM_DURATION: Duration = 800.milliseconds
    val MAP_CAMERA_ANIM_DURATION_S = MAP_CAMERA_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val MAP_FOLLOW_ANIM_DURATION: Duration = 500.milliseconds
    val MAP_FOLLOW_ANIM_DURATION_S = MAP_FOLLOW_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val NAVIGATION_TRANSITION_DURATION = 300.milliseconds
}
