package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult

interface GeocodingRepository {
    suspend fun autocomplete(searchText: String): NetworkResult<List<LocationIqPlace>>

    suspend fun reverseGeocode(location: Location): NetworkResult<LocationIqPlace?>
}
