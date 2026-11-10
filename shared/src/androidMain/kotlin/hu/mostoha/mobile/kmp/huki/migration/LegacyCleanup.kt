package hu.mostoha.mobile.kmp.huki.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.datastore.SupportPreferenceKeys
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Deletes what the legacy HuKi-Android app left behind under the same application ID: its Room database, its
 * settings DataStore and the OsmDroid tile cache. GPX files under `files/gpx` are shared with this app and kept.
 *
 * Runs once: a failed delete is reported and left in place, it never conflicts with this app's files.
 */
class LegacyCleanup(
    private val dataDir: File,
    private val externalFilesDirs: () -> List<File>,
    private val dataStore: DataStore<Preferences>,
    private val crashlyticsService: CrashlyticsService,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend fun run() {
        withContext(dispatcher) {
            try {
                cleanUp()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(exception) { "Legacy: Cleanup failed." }
                crashlyticsService.recordException(exception)
            }
        }
    }

    private suspend fun cleanUp() {
        // Reading the settings runs LegacySupportMigration, which needs the legacy DataStore
        val settings = dataStore.data.first()
        if (settings[CLEANUP_DONE] == true) return
        if (settings[SupportPreferenceKeys.LEGACY_MIGRATED] != true) {
            Logger.w { "Legacy: Support purchases not migrated yet, cleanup postponed." }
            return
        }
        val legacyFiles = legacyFiles().filter { it.exists() }
        if (legacyFiles.isNotEmpty()) {
            deleteAll(legacyFiles)
        }
        dataStore.edit { it[CLEANUP_DONE] = true }
    }

    private fun legacyFiles(): List<File> {
        val databasesDir = dataDir.resolve("databases")
        val osmDroidDirs = (listOf(dataDir.resolve("files")) + externalFilesDirs()).map { it.resolve(OSMDROID_DIR) }
        return listOf(
            databasesDir.resolve(DATABASE_NAME),
            databasesDir.resolve("$DATABASE_NAME-wal"),
            databasesDir.resolve("$DATABASE_NAME-shm"),
            databasesDir.resolve("$DATABASE_NAME-journal"),
            dataDir.resolve("cache/$DATABASE_NAME.lck"),
            dataDir.resolve(DATA_STORE_FILE_PATH),
            dataDir.resolve(OSMDROID_SHARED_PREFS_FILE_PATH),
        ) + osmDroidDirs
    }

    private fun deleteAll(items: List<File>) {
        val sizeBefore = items.sumOf { it.sizeInBytes() }
        val failed = items.filterNot { it.deleteRecursively() }
        val freedBytes = sizeBefore - failed.sumOf { it.sizeInBytes() }
        val message = "Legacy: Cleanup freed ${freedBytes / BYTES_PER_KB} KB, deleted ${items.size - failed.size} " +
            "of ${items.size} items."
        Logger.i { message }
        crashlyticsService.log(message)
        if (failed.isNotEmpty()) {
            crashlyticsService.recordException(IOException("Legacy: Failed to delete ${failed.map { it.name }}"))
        }
    }

    private fun File.sizeInBytes(): Long = walkBottomUp().filter { it.isFile }.sumOf { it.length() }

    companion object {
        private const val DATABASE_NAME = "huki-database"
        private const val DATA_STORE_FILE_PATH = "files/datastore/user_preferences.preferences_pb"
        private const val OSMDROID_SHARED_PREFS_FILE_PATH = "shared_prefs/KEY_GLOBAL_SHARED_PREFERENCES.xml"
        private const val OSMDROID_DIR = "osmdroid"
        private const val BYTES_PER_KB = 1024

        private val CLEANUP_DONE = booleanPreferencesKey("legacy_cleanup_done")
    }
}
