package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.database.PlaceHistoryDao
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryItem
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlace
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceHistoryEntity
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceHistoryItem
import hu.mostoha.mobile.kmp.huki.util.NameNormalizer
import hu.mostoha.mobile.kmp.huki.util.escapeLikeWildcards
import kotlin.time.Clock

class DefaultPlaceHistoryRepository(
    private val placeHistoryDao: PlaceHistoryDao,
    private val clock: Clock,
) : PlaceHistoryRepository {
    override suspend fun recordVisit(place: Place) {
        placeHistoryDao.upsert(place.toPlaceHistoryEntity(clock.now().toEpochMilliseconds()))
    }

    override suspend fun getRecentPlaces(limit: Int): List<Place> =
        placeHistoryDao.getRecent(limit).map { it.toPlace() }

    override suspend fun searchPlaces(query: String, limit: Int): List<Place> {
        val normalizedQuery = NameNormalizer.normalize(query.trim()).escapeLikeWildcards()
        if (normalizedQuery.isEmpty()) return emptyList()
        return placeHistoryDao.searchByName(normalizedQuery, limit).map { it.toPlace() }
    }

    override suspend fun getPlaceHistory(): List<PlaceHistoryItem> =
        placeHistoryDao.getAll().map { it.toPlaceHistoryItem() }

    override suspend fun getPlace(osmType: OsmType, osmId: String): Place? =
        placeHistoryDao.getByKey(osmType, osmId)?.toPlace()
}
