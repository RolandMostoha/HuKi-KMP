package hu.mostoha.mobile.kmp.huki.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import hu.mostoha.mobile.kmp.huki.model.db.PlaceHistoryEntity

@Database(entities = [PlaceHistoryEntity::class], version = 1)
@ConstructedBy(HukiDatabaseConstructor::class)
abstract class HukiDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "huki.db"
    }

    abstract fun placeHistoryDao(): PlaceHistoryDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object HukiDatabaseConstructor : RoomDatabaseConstructor<HukiDatabase> {
    override fun initialize(): HukiDatabase
}
