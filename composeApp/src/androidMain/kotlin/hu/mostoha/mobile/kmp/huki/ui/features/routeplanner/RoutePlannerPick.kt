package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import hu.mostoha.mobile.kmp.huki.model.domain.Location

/**
 * The [id] makes every pick distinct, so long-tapping the same coordinate twice still adds two stops.
 */
data class RoutePlannerPick(
    val id: Long,
    val location: Location,
)
