package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiEvents

/**
 * A map interaction for the planner, where the [id] makes every one distinct, so long-tapping the same coordinate
 * twice still adds two stops.
 */
data class RoutePlannerMapEvent(
    val id: Long,
    val event: RoutePlannerUiEvents,
)
