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

    val EXPAND_ANIM_DURATION: Duration = 250.milliseconds
    val EXPAND_ANIM_DURATION_S = EXPAND_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val SELECTION_ANIM_DURATION: Duration = 150.milliseconds
    val SELECTION_ANIM_DURATION_S = SELECTION_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val STATE_CHANGE_ANIM_DURATION: Duration = 300.milliseconds
    val STATE_CHANGE_ANIM_DURATION_S = STATE_CHANGE_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val TOGGLE_ANIM_DURATION: Duration = 200.milliseconds
    val TOGGLE_ANIM_DURATION_S = TOGGLE_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val MENU_EXPAND_ANIM_DURATION: Duration = 350.milliseconds
    val MENU_EXPAND_ANIM_DURATION_S = MENU_EXPAND_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val FAB_REVEAL_ANIM_DURATION: Duration = 250.milliseconds
    val FAB_REVEAL_ANIM_DURATION_S = FAB_REVEAL_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val PRESS_FEEDBACK_ANIM_DURATION: Duration = 120.milliseconds
    val PRESS_FEEDBACK_ANIM_DURATION_S = PRESS_FEEDBACK_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val COLOR_SCHEME_ANIM_DURATION: Duration = 200.milliseconds
    val COLOR_SCHEME_ANIM_DURATION_S = COLOR_SCHEME_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val DISMISS_ANIM_DURATION: Duration = 200.milliseconds
    val DISMISS_ANIM_DURATION_S = DISMISS_ANIM_DURATION.toDouble(DurationUnit.SECONDS)

    val SEARCH_CONTENT_FADE_IN_DURATION: Duration = 220.milliseconds
    val SEARCH_CONTENT_FADE_OUT_DURATION: Duration = 90.milliseconds
}
