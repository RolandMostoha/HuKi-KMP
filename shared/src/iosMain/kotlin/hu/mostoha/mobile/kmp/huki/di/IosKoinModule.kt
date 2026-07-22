package hu.mostoha.mobile.kmp.huki.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.icerock.moko.permissions.ios.PermissionsController
import dev.icerock.moko.permissions.ios.PermissionsControllerProtocol
import hu.mostoha.mobile.kmp.huki.database.HukiDatabase
import hu.mostoha.mobile.kmp.huki.datastore.SETTINGS_DATA_STORE_FILE_NAME
import hu.mostoha.mobile.kmp.huki.datastore.createDataStore
import hu.mostoha.mobile.kmp.huki.db.documentDirectoryPath
import hu.mostoha.mobile.kmp.huki.features.destinations.DestinationsViewModel
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionViewModel
import hu.mostoha.mobile.kmp.huki.features.gpxtutorial.GpxTutorialViewModel
import hu.mostoha.mobile.kmp.huki.features.locationiq.LocationIqViewModel
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.features.menu.MenuViewModel
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.kmp.huki.features.placehistory.PlaceHistoryViewModel
import hu.mostoha.mobile.kmp.huki.features.settings.SettingsViewModel
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsDecoratorService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.IosLocationMonitoringService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module

val iosPlatformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<PermissionsControllerProtocol> { PermissionsController() }
    single<LocationMonitoringService> {
        IosLocationMonitoringService(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
        )
    }
    single<RoomDatabase.Builder<HukiDatabase>> {
        Room.databaseBuilder<HukiDatabase>(name = documentDirectoryPath() + "/${HukiDatabase.DATABASE_NAME}")
    }
    single<DataStore<Preferences>> {
        createDataStore { documentDirectoryPath() + "/$SETTINGS_DATA_STORE_FILE_NAME" }
    }
}

fun initKoin(analyticsService: AnalyticsService, crashlyticsService: CrashlyticsService) {
    initKoin {
        modules(iosPlatformModule)
        modules(module { single { crashlyticsService } })
        modules(
            module {
                single<AnalyticsService> {
                    CrashlyticsDecoratorService(analyticsService, crashlyticsService)
                }
            },
        )
    }
}

object KoinViewModelProvider : KoinComponent {
    fun getMainViewModel(): MainViewModel = get()
    fun getPlaceFinderViewModel(): PlaceFinderViewModel = get()
    fun getMenuViewModel(): MenuViewModel = get()
    fun getGpxCollectionViewModel(): GpxCollectionViewModel = get()
    fun getGpxTutorialViewModel(): GpxTutorialViewModel = get()
    fun getLocationIqViewModel(): LocationIqViewModel = get()
    fun getPlaceHistoryViewModel(): PlaceHistoryViewModel = get()
    fun getSettingsViewModel(): SettingsViewModel = get()
    fun getDestinationsViewModel(): DestinationsViewModel = get()
}
