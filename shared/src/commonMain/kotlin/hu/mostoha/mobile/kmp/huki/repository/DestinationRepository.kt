package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import org.maplibre.spatialk.units.Length

interface DestinationRepository {
    fun getTopDestinations(limit: Int = DEFAULT_DESTINATIONS_LIMIT): List<Destination>

    fun getPopularDestinations(): List<Destination>

    fun getNearbyDestinations(
        location: Location,
        radius: Length? = null,
        limit: Int = Int.MAX_VALUE,
    ): List<Destination>

    fun searchDestinations(query: String, limit: Int): List<Destination>

    fun requireDestination(osmId: String): Destination

    fun getLandscapes(): List<Landscape>

    companion object {
        const val DEFAULT_DESTINATIONS_LIMIT = 20
    }
}
