package hu.mostoha.mobile.kmp.huki.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import hu.mostoha.mobile.kmp.huki.TestContext.appContext
import hu.mostoha.mobile.kmp.huki.database.migration.Migration1To2
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class HukiDatabaseMigrationTest {

    private val databasePath = appContext.getDatabasePath(TEST_DATABASE_NAME).absolutePath

    @Before
    @After
    fun deleteDatabase() {
        appContext.deleteDatabase(TEST_DATABASE_NAME)
    }

    @Test
    fun givenLegacyDestinationHistory_whenMigrating1To2_thenRowIsRekeyedToDestinationId() {
        runTest {
            createVersion1Database(
                insertRow(osmId = "1158963147", name = "Fátyol-vízesés", placeSource = PlaceSource.DESTINATIONS),
            )

            val dao = openMigratedDatabase().placeHistoryDao()

            val migrated = dao.getByKey(OsmType.NODE, "szilvasvarad_fatyol_vizeses").shouldNotBeNull()
            migrated.name shouldBe "Fátyol-vízesés"
            migrated.lastVisited shouldBe LAST_VISITED
            dao.getByKey(OsmType.NODE, "1158963147").shouldBeNull()
        }
    }

    @Test
    fun givenSearchHistoryWithLegacyId_whenMigrating1To2_thenRowIsUnchanged() {
        runTest {
            createVersion1Database(
                insertRow(osmId = "1158963147", name = "Fátyol-vízesés", placeSource = PlaceSource.SEARCH_AUTOCOMPLETE),
            )

            val dao = openMigratedDatabase().placeHistoryDao()

            dao.getByKey(OsmType.NODE, "1158963147").shouldNotBeNull()
            dao.getByKey(OsmType.NODE, "szilvasvarad_fatyol_vizeses").shouldBeNull()
        }
    }

    @Test
    fun givenRemovedDestinationHistory_whenMigrating1To2_thenRowIsRekeyed() {
        runTest {
            createVersion1Database(
                insertRow(osmId = "258397175", name = "Noszvaj", placeSource = PlaceSource.DESTINATIONS),
            )

            val dao = openMigratedDatabase().placeHistoryDao()

            dao.getByKey(OsmType.NODE, "noszvaj").shouldNotBeNull()
        }
    }

    private fun createVersion1Database(vararg statements: String) {
        BundledSQLiteDriver().open(databasePath).use { connection ->
            connection.execSQL(CREATE_TABLE_VERSION_1)
            statements.forEach { connection.execSQL(it) }
            connection.execSQL("PRAGMA user_version = 1")
        }
    }

    private fun openMigratedDatabase(): HukiDatabase =
        Room.databaseBuilder<HukiDatabase>(context = appContext, name = databasePath)
            .setDriver(BundledSQLiteDriver())
            .addMigrations(Migration1To2)
            .build()

    private fun insertRow(osmId: String, name: String, placeSource: PlaceSource): String =
        "INSERT INTO place_history (osmType, osmId, name, nameNormalized, latitude, longitude, placeSource, " +
            "lastVisited) VALUES ('${OsmType.NODE.name}', '$osmId', '$name', '$name', 48.07, 20.41, " +
            "'${placeSource.name}', $LAST_VISITED)"

    companion object {
        private const val TEST_DATABASE_NAME = "huki-migration-test.db"
        private const val LAST_VISITED = 1_700_000_000_000L
        private const val CREATE_TABLE_VERSION_1 =
            "CREATE TABLE IF NOT EXISTS `place_history` (`osmType` TEXT NOT NULL, `osmId` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, `nameNormalized` TEXT NOT NULL, `address` TEXT, `latitude` REAL NOT NULL, " +
                "`longitude` REAL NOT NULL, `placeCategory` TEXT, `placeSource` TEXT NOT NULL, " +
                "`lastVisited` INTEGER NOT NULL, `bounding_box_north` REAL, `bounding_box_east` REAL, " +
                "`bounding_box_south` REAL, `bounding_box_west` REAL, PRIMARY KEY(`osmType`, `osmId`))"
    }
}
