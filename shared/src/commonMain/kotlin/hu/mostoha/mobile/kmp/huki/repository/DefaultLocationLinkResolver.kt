package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink
import hu.mostoha.mobile.kmp.huki.model.domain.LocationLink
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceSearchResult
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.util.LocationLinkParser

class DefaultLocationLinkResolver(private val geocodingRepository: GeocodingRepository) : LocationLinkResolver {
    override suspend fun resolve(link: IncomingLink): LocationLinkResult =
        when (val locationLink = LocationLinkParser.parse(link.text)) {
            null -> LocationLinkResult.Unsupported
            is LocationLink.Coordinates -> LocationLinkResult.Coordinates(locationLink.location)
            is LocationLink.PlaceName -> searchPlace(locationLink.name)
        }

    private suspend fun searchPlace(name: String): LocationLinkResult =
        when (val result = geocodingRepository.autocomplete(name)) {
            is NetworkResult.Success -> result.data.firstOrNull()
                ?.let { LocationLinkResult.PlaceFound(it.toPlaceSearchResult()) }
                ?: LocationLinkResult.Unsupported
            is NetworkResult.Error -> if (result.error == NetworkError.NOT_FOUND) {
                LocationLinkResult.Unsupported
            } else {
                LocationLinkResult.Failed(result.error)
            }
        }
}
