package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.data.ALL_DESTINATIONS
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.math.min
import kotlin.random.Random

/**
 * Ranks destinations by a weighted blend of
 * - popularity
 * - distance (when a location is known)
 * - random jitter -> for the surprise effect
 * - type-diversity penalty so categories don't cluster (e.g. avoid 3 PEAKs in a row).
 */
class DefaultDestinationRepository(private val random: Random = Random.Default) : DestinationRepository {

    private companion object {
        const val MAX_POPULARITY = 10.0
        const val DISTANCE_HALF_LIFE_KM = 40.0

        const val WEIGHT_POPULARITY = 0.3
        const val WEIGHT_DISTANCE = 0.6
        const val WEIGHT_RANDOM = 0.1

        const val WEIGHT_POPULARITY_NO_LOCATION = 0.65
        const val WEIGHT_RANDOM_NO_LOCATION = 0.35

        const val TYPE_PENALTY = 0.12
        const val MAX_PENALTY_STEPS = 3
    }

    // Ranking is frozen once for the session, but only after a location was available, so a cold
    // start without a fix doesn't permanently cache a distance-less order.
    private var rankedDestinations: List<Destination>? = null

    override fun getTopDestinations(location: Location?, limit: Int): List<Destination> {
        rankedDestinations?.let { return it.take(limit) }

        val ranked = rankDestinations(location)
        if (location != null) {
            rankedDestinations = ranked
        }

        return ranked.take(limit)
    }

    private fun rankDestinations(location: Location?): List<Destination> {
        val baseScores = ALL_DESTINATIONS.associateWith { baseScore(it, location) }

        val remaining = ALL_DESTINATIONS.toMutableList()
        val typeCounts = mutableMapOf<DestinationType, Int>()
        val ordered = ArrayList<Destination>(remaining.size)

        while (remaining.isNotEmpty()) {
            val next = remaining.maxBy { destination ->
                val penaltySteps = min(typeCounts[destination.type] ?: 0, MAX_PENALTY_STEPS)
                baseScores.getValue(destination) - TYPE_PENALTY * penaltySteps
            }
            ordered += next
            remaining -= next
            typeCounts[next.type] = (typeCounts[next.type] ?: 0) + 1
        }

        return ordered
    }

    private fun baseScore(destination: Destination, location: Location?): Double {
        val popularityScore = destination.popularity / MAX_POPULARITY
        val randomScore = random.nextDouble()

        return if (location != null) {
            val km = location.distanceBetween(destination.location).inMeters / 1000.0
            val distanceScore = 1.0 / (1.0 + km / DISTANCE_HALF_LIFE_KM)
            WEIGHT_POPULARITY * popularityScore +
                WEIGHT_DISTANCE * distanceScore +
                WEIGHT_RANDOM * randomScore
        } else {
            WEIGHT_POPULARITY_NO_LOCATION * popularityScore + WEIGHT_RANDOM_NO_LOCATION * randomScore
        }
    }
}
