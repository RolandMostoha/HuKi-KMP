package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.RoutingMode

object FeatureFlags {
    /**
     *  Flags for not completed features.
     */
    const val IS_GPX_RENAME_ENABLED = false

    /**
     * Debug only flags.
     */
    const val DEBUG_ALWAYS_SHOW_WHATSNEW = false
    const val DEBUG_ENABLE_ANALYTICS = false
    const val DEBUG_ENABLE_CRASHLYTICS = false
    const val DEBUG_SHOW_CAMERA_PANEL = false
    val DEBUG_ROUTING_MODE = RoutingMode.AUTO
}
