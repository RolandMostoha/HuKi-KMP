package hu.mostoha.mobile.kmp.huki.features.routeplanner

import hu.mostoha.mobile.kmp.huki.model.domain.RoundTripOptions
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerMode
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants

/**
 * The wizard starts from my location, which its location fix fills in.
 */
fun RoutePlannerUiState.Companion.roundTripWizard(options: RoundTripOptions): RoutePlannerUiState =
    RoutePlannerUiState(
        waypoints = listOf(RoutePlannerUiState.MY_LOCATION_WAYPOINT, RoutePlannerWaypoint()),
        mode = RoutePlannerMode.ROUND_TRIP_WIZARD,
        roundTrip = options,
    )

/**
 * Keeps the seed and heading, so only the loop's length changes.
 */
fun RoutePlannerUiState.withRoundTripDistance(distanceKm: Int): RoutePlannerUiState {
    val options = roundTrip ?: return this
    val clamped = distanceKm.coerceIn(
        RoutePlanConstants.ROUND_TRIP_MIN_DISTANCE_KM,
        RoutePlanConstants.ROUND_TRIP_MAX_DISTANCE_KM,
    )
    return copy(roundTrip = options.copy(distanceKm = clamped))
}
