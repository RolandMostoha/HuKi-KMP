package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.data.ALL_DESTINATIONS
import hu.mostoha.mobile.kmp.huki.data.Landscapes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.NameNormalizer
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.math.min
import kotlin.random.Random

/**
 * Ranks destinations by a weighted blend of
 * - popularity
 * - random jitter -> for the surprise effect
 * - type-diversity penalty so categories don't cluster (e.g. avoid 3 PEAKs in a row).
 */
class DefaultDestinationRepository(private val random: Random = Random.Default) : DestinationRepository {

    private companion object {
        const val MAX_POPULARITY = 10.0

        const val WEIGHT_POPULARITY = 0.65
        const val WEIGHT_RANDOM = 0.35

        const val TYPE_PENALTY = 0.12
        const val MAX_PENALTY_STEPS = 3
    }

    override fun getTopDestinations(limit: Int): List<Destination> = rankDestinations().take(limit)

    override fun getPopularDestinations(): List<Destination> =
        ALL_DESTINATIONS.sortedWith(compareByDescending<Destination> { it.popularity }.thenBy { it.name })

    override fun getNearbyDestinations(location: Location, radius: Length?, limit: Int): List<Destination> =
        ALL_DESTINATIONS
            .map { it to it.location.distanceBetween(location).inMeters }
            .filter { (_, distance) -> radius == null || distance <= radius.inMeters }
            .sortedBy { (_, distance) -> distance }
            .take(limit)
            .map { (destination, _) -> destination }

    override fun searchDestinations(query: String, limit: Int): List<Destination> {
        val normalizedQuery = NameNormalizer.normalize(query.trim())
        if (normalizedQuery.isEmpty()) return emptyList()
        return ALL_DESTINATIONS
            .filter { destination ->
                NameNormalizer.normalize(destination.name).contains(normalizedQuery) ||
                    NameNormalizer.normalize(destination.town).contains(normalizedQuery)
            }
            .sortedWith(compareByDescending<Destination> { it.popularity }.thenBy { it.name })
            .take(limit)
    }

    override fun requireDestination(osmId: String): Destination = ALL_DESTINATIONS.first { it.osmId == osmId }

    override fun getLandscapes(): List<Landscape> = Landscapes

    private fun rankDestinations(): List<Destination> {
        val baseScores = ALL_DESTINATIONS.associateWith { baseScore(it) }

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

    private fun baseScore(destination: Destination): Double =
        WEIGHT_POPULARITY * (destination.popularity / MAX_POPULARITY) + WEIGHT_RANDOM * random.nextDouble()
}
