package hu.mostoha.mobile.kmp.huki.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import hu.mostoha.mobile.kmp.huki.model.db.PlaceHistoryEntity
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceHistoryDao {
    @Upsert
    suspend fun upsert(entity: PlaceHistoryEntity)

    @Query("SELECT * FROM place_history ORDER BY lastVisited DESC")
    fun observeAll(): Flow<List<PlaceHistoryEntity>>

    @Query("SELECT * FROM place_history ORDER BY lastVisited DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<PlaceHistoryEntity>

    @Query("SELECT * FROM place_history ORDER BY lastVisited DESC")
    suspend fun getAll(): List<PlaceHistoryEntity>

    @Query("SELECT * FROM place_history WHERE osmType = :osmType AND osmId = :osmId LIMIT 1")
    suspend fun getByKey(osmType: OsmType, osmId: String): PlaceHistoryEntity?
}
