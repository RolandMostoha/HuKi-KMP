package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError

interface LocationLinkResolver {
    suspend fun resolve(link: IncomingLink.Text): LocationLinkResult
}

sealed interface LocationLinkResult {
    data class Coordinates(val location: Location) : LocationLinkResult

    data class PlaceFound(val place: Place) : LocationLinkResult

    data object Unsupported : LocationLinkResult

    data class Failed(val error: NetworkError) : LocationLinkResult
}
