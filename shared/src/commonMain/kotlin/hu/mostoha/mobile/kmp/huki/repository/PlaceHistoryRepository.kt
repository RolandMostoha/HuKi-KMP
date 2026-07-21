package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryItem

interface PlaceHistoryRepository {
    suspend fun recordVisit(place: Place)

    suspend fun recordVisit(destination: Destination)

    suspend fun getRecentPlaces(limit: Int): List<Place>

    suspend fun searchPlaces(query: String, limit: Int): List<Place>

    suspend fun getPlaceHistory(): List<PlaceHistoryItem>

    suspend fun getPlace(osmType: OsmType, osmId: String): Place?
}
