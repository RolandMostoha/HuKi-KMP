package hu.mostoha.mobile.kmp.huki.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.icerock.moko.permissions.PermissionsController
import hu.mostoha.mobile.kmp.huki.database.HukiDatabase
import hu.mostoha.mobile.kmp.huki.datastore.LegacySupportMigration
import hu.mostoha.mobile.kmp.huki.datastore.SETTINGS_DATA_STORE_FILE_NAME
import hu.mostoha.mobile.kmp.huki.datastore.createDataStore
import hu.mostoha.mobile.kmp.huki.migration.LegacyCleanup
import hu.mostoha.mobile.kmp.huki.repository.AndroidBundledFileReader
import hu.mostoha.mobile.kmp.huki.repository.BundledFileReader
import hu.mostoha.mobile.kmp.huki.service.AndroidLocationMonitoringService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Buffer
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * The preferences file of the legacy HuKi-Android app, under the same application ID.
 */
private const val LEGACY_DATA_STORE_FILE_PATH = "datastore/user_preferences.preferences_pb"

val androidPlatformModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { PermissionsController(applicationContext = get()) }
    single<RoomDatabase.Builder<HukiDatabase>> {
        val context = get<Context>()
        Room.databaseBuilder<HukiDatabase>(
            context = context,
            name = context.getDatabasePath(HukiDatabase.DATABASE_NAME).absolutePath,
        )
    }
    single<DataStore<Preferences>> {
        val context = get<Context>()
        val legacyFile = context.filesDir.resolve(LEGACY_DATA_STORE_FILE_PATH)
        createDataStore(
            migrations = listOf(
                LegacySupportMigration {
                    if (legacyFile.exists()) {
                        PreferencesSerializer.readFrom(
                            Buffer().write(legacyFile.readBytes()),
                        )
                    } else {
                        null
                    }
                },
            ),
        ) { context.filesDir.resolve(SETTINGS_DATA_STORE_FILE_NAME).absolutePath }
    }
    single<BundledFileReader> { AndroidBundledFileReader(context = get()) }
    single<LocationMonitoringService> {
        AndroidLocationMonitoringService(
            context = get(),
            crashlyticsService = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )
    }
    single {
        val context = get<Context>()
        LegacyCleanup(
            dataDir = context.dataDir,
            externalFilesDirs = { context.getExternalFilesDirs(null).filterNotNull() },
            dataStore = get(),
            crashlyticsService = get(),
            dispatcher = get(named(Dispatcher.IO)),
        )
    }
}
