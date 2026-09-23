package hu.mostoha.mobile.kmp.huki.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
import hu.mostoha.mobile.kmp.huki.features.gpxguide.GpxGuideViewModel
import hu.mostoha.mobile.kmp.huki.features.locationiq.LocationIqViewModel
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.features.menu.MenuViewModel
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.kmp.huki.features.placehistory.PlaceHistoryViewModel
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerViewModel
import hu.mostoha.mobile.kmp.huki.features.settings.SettingsViewModel
import hu.mostoha.mobile.kmp.huki.features.trailsymbolsguide.TrailSymbolsGuideViewModel
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
    fun getPlaceFinderViewModel(owner: IosViewModelStoreOwner): PlaceFinderViewModel = owner.resolve()
    fun getMenuViewModel(owner: IosViewModelStoreOwner): MenuViewModel = owner.resolve()
    fun getGpxCollectionViewModel(owner: IosViewModelStoreOwner): GpxCollectionViewModel = owner.resolve()
    fun getGpxGuideViewModel(owner: IosViewModelStoreOwner): GpxGuideViewModel = owner.resolve()
    fun getTrailSymbolsGuideViewModel(owner: IosViewModelStoreOwner): TrailSymbolsGuideViewModel = owner.resolve()
    fun getLocationIqViewModel(owner: IosViewModelStoreOwner): LocationIqViewModel = owner.resolve()
    fun getPlaceHistoryViewModel(owner: IosViewModelStoreOwner): PlaceHistoryViewModel = owner.resolve()
    fun getRoutePlannerViewModel(): RoutePlannerViewModel = get()
    fun getSettingsViewModel(owner: IosViewModelStoreOwner): SettingsViewModel = owner.resolve()
    fun getDestinationsViewModel(owner: IosViewModelStoreOwner): DestinationsViewModel = owner.resolve()

    private inline fun <reified T : ViewModel> ViewModelStoreOwner.resolve(): T =
        ViewModelProvider.create(this, viewModelFactory { initializer { get<T>() } })[T::class]
}
