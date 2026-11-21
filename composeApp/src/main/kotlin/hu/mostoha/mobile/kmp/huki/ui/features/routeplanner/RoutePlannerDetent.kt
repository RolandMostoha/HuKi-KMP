package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

enum class RoutePlannerDetent {
    // Keeps only the title, stats and save button
    MINIMIZED,

    // Fits all stops up to a height cap, past which the stop list scrolls
    EXPANDED,
    ;

    fun toggled(): RoutePlannerDetent = if (this == MINIMIZED) EXPANDED else MINIMIZED
}
