package hu.mostoha.mobile.kmp.huki.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import hu.mostoha.mobile.kmp.huki.datastore.SETTINGS_DATA_STORE_FILE_NAME
import hu.mostoha.mobile.kmp.huki.datastore.SupportPreferenceKeys
import hu.mostoha.mobile.kmp.huki.datastore.createDataStore
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test

class LegacyCleanupTest {

    private val dataDir = createTempDirectory("legacy-cleanup").toFile()
    private val externalFilesDir = createTempDirectory("legacy-cleanup-external").toFile()
    private val dataStore = createDataStore { dataDir.resolve("files/$SETTINGS_DATA_STORE_FILE_NAME").absolutePath }
    private val crashlyticsService = FakeCrashlyticsService()
    private val cleanup = LegacyCleanup(
        dataDir = dataDir,
        externalFilesDirs = { listOf(externalFilesDir) },
        dataStore = dataStore,
        crashlyticsService = crashlyticsService,
        dispatcher = Dispatchers.Unconfined,
    )

    @AfterTest
    fun tearDown() {
        dataDir.resolve("databases").setWritable(true)
        dataDir.deleteRecursively()
        externalFilesDir.deleteRecursively()
    }

    @Test
    fun `Given migrated support purchases - When run - Then legacy files are deleted and app files are kept`() =
        runTest {
            dataStore.markSupportMigrated()
            val legacyFiles = seedLegacyFiles()
            val appFiles = seedAppFiles()

            cleanup.run()

            legacyFiles.filter { it.exists() }.shouldBeEmpty()
            appFiles.filterNot { it.exists() }.shouldBeEmpty()
            crashlyticsService.recordedExceptions.shouldBeEmpty()
            crashlyticsService.logs.single() shouldStartWith "Legacy: Cleanup freed"
        }

    @Test
    fun `Given support purchases not migrated - When run - Then the legacy files are kept`() =
        runTest {
            val legacyFiles = seedLegacyFiles()

            cleanup.run()

            legacyFiles.filterNot { it.exists() }.shouldBeEmpty()
        }

    @Test
    fun `Given a completed cleanup - When run again - Then nothing is deleted`() =
        runTest {
            dataStore.markSupportMigrated()
            cleanup.run()
            val osmDroidDir = dataDir.seedDir("files/osmdroid")

            cleanup.run()

            osmDroidDir.exists() shouldBe true
        }

    @Test
    fun `Given an undeletable legacy file - When run twice - Then it is reported once and left in place`() =
        runTest {
            dataStore.markSupportMigrated()
            val database = dataDir.seedFile("databases/huki-database")
            dataDir.resolve("databases").setWritable(false)

            cleanup.run()
            cleanup.run()

            database.exists() shouldBe true
            crashlyticsService.recordedExceptions shouldHaveSize 1
        }

    @Test
    fun `Given no legacy files - When run - Then nothing is logged`() =
        runTest {
            dataStore.markSupportMigrated()

            cleanup.run()

            crashlyticsService.logs.shouldBeEmpty()
            crashlyticsService.recordedExceptions.shouldBeEmpty()
        }

    private suspend fun DataStore<Preferences>.markSupportMigrated() {
        edit { it[SupportPreferenceKeys.LEGACY_MIGRATED] = true }
    }

    private fun seedLegacyFiles(): List<File> =
        listOf(
            dataDir.seedFile("databases/huki-database"),
            dataDir.seedFile("databases/huki-database-wal"),
            dataDir.seedFile("databases/huki-database-shm"),
            dataDir.seedFile("databases/huki-database-journal"),
            dataDir.seedFile("cache/huki-database.lck"),
            dataDir.seedFile("files/datastore/user_preferences.preferences_pb"),
            dataDir.seedFile("shared_prefs/KEY_GLOBAL_SHARED_PREFERENCES.xml"),
            dataDir.seedDir("files/osmdroid"),
            externalFilesDir.seedDir("osmdroid"),
        )

    private fun seedAppFiles(): List<File> =
        listOf(
            dataDir.seedFile("databases/huki.db"),
            dataDir.seedFile("files/gpx/external/legacy.gpx"),
            dataDir.seedFile("files/gpx/routeplanner/route.gpx"),
            dataDir.seedFile("files/datastore/firebase_session_settings.preferences_pb"),
            dataDir.seedFile("shared_prefs/com.google.firebase.crashlytics.xml"),
        )

    private fun File.seedFile(path: String): File =
        resolve(path).apply {
            parentFile.mkdirs()
            writeText("legacy")
        }

    private fun File.seedDir(path: String): File = resolve(path).apply { seedFile("tiles/cache.db") }
}
