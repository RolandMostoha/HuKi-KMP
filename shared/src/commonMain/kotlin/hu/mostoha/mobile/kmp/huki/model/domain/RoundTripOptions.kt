package hu.mostoha.mobile.kmp.huki.model.domain

/**
 * A round trip's requested length, and the seed and heading that pick one of the loops of that length.
 */
data class RoundTripOptions(
    val distanceKm: Int,
    val seed: Int,
    val heading: Int,
)
