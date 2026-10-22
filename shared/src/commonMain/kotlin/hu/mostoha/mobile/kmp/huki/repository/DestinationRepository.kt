package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationCategory
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationChallenge
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.mapper.toDestinationCategory
import org.maplibre.spatialk.units.Length

interface DestinationRepository {
    fun getTopDestinations(limit: Int = DEFAULT_DESTINATIONS_LIMIT): List<Destination>

    fun getPopularDestinations(): List<Destination>

    fun getDestinationsByType(type: DestinationType): List<Destination> =
        getPopularDestinations().filter { it.type == type }

    fun getDestinationsByChallenge(challenge: DestinationChallenge): List<Destination> =
        getPopularDestinations().filter { challenge in it.challenges }

    fun getDestinationCategories(): List<DestinationCategory> {
        val challenges = DestinationChallenge.entries.map { challenge ->
            challenge.toDestinationCategory(getDestinationsByChallenge(challenge))
        }
        val types = DestinationType.entries.map { type ->
            type.toDestinationCategory(getDestinationsByType(type))
        }
        return (challenges + types).filter { it.destinations.isNotEmpty() }
    }

    fun getNearbyDestinations(
        location: Location,
        radius: Length? = null,
        limit: Int = Int.MAX_VALUE,
    ): List<Destination>

    fun searchDestinations(query: String, limit: Int): List<Destination>

    fun requireDestination(id: String): Destination

    fun getLandscapes(): List<Landscape>

    companion object {
        const val DEFAULT_DESTINATIONS_LIMIT = 20
    }
}
