package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.database.PlaceHistoryDao
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryItem
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlace
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceHistoryEntity
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceHistoryItem
import kotlin.time.Clock

class DefaultPlaceHistoryRepository(
    private val placeHistoryDao: PlaceHistoryDao,
    private val clock: Clock,
) : PlaceHistoryRepository {
    override suspend fun recordVisit(place: Place) {
        placeHistoryDao.upsert(place.toPlaceHistoryEntity(clock.now().toEpochMilliseconds()))
    }

    override suspend fun recordVisit(destination: Destination) {
        placeHistoryDao.upsert(destination.toPlaceHistoryEntity(clock.now().toEpochMilliseconds()))
    }

    override suspend fun getRecentPlaces(limit: Int): List<Place> =
        placeHistoryDao.getRecent(limit).map { it.toPlace() }

    override suspend fun getPlaceHistory(): List<PlaceHistoryItem> =
        placeHistoryDao.getAll().map { it.toPlaceHistoryItem() }

    override suspend fun getPlace(osmType: OsmType, osmId: String): Place? =
        placeHistoryDao.getByKey(osmType, osmId)?.toPlace()
}
