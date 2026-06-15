package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.Location

interface DestinationRepository {
    fun getTopDestinations(location: Location? = null, limit: Int = DEFAULT_TOP_DESTINATIONS_LIMIT): List<Destination>

    companion object {
        const val DEFAULT_TOP_DESTINATIONS_LIMIT = 20
    }
}
