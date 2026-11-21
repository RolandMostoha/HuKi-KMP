package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.RoundTripOptions
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Draws another loop of [distanceKm]. The seed always differs from [previous]'s, so a regenerated round trip is a new
 * request.
 */
fun Random.nextRoundTripOptions(distanceKm: Int, previous: RoundTripOptions? = null): RoundTripOptions {
    var seed = nextInt(RoutePlanConstants.ROUND_TRIP_SEED_RANGE)
    while (seed == previous?.seed) {
        seed = nextInt(RoutePlanConstants.ROUND_TRIP_SEED_RANGE)
    }
    return RoundTripOptions(
        distanceKm = distanceKm,
        seed = seed,
        heading = nextInt(RoutePlanConstants.ROUND_TRIP_HEADING_RANGE),
    )
}
