package hu.mostoha.mobile.kmp.huki.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import hu.mostoha.mobile.kmp.huki.database.HukiDatabase
import hu.mostoha.mobile.kmp.huki.features.destinations.DestinationsViewModel
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionViewModel
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.features.menu.MenuViewModel
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.kmp.huki.features.placehistory.PlaceHistoryViewModel
import hu.mostoha.mobile.kmp.huki.network.createHttpClient
import hu.mostoha.mobile.kmp.huki.repository.DefaultDestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.DefaultGpxMetadataStore
import hu.mostoha.mobile.kmp.huki.repository.DefaultGpxRepository
import hu.mostoha.mobile.kmp.huki.repository.DefaultGpxStorage
import hu.mostoha.mobile.kmp.huki.repository.DefaultPlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxMetadataStore
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxStorage
import hu.mostoha.mobile.kmp.huki.repository.LocationIqGeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.time.Clock

val appModule = module {
    single<GpxStorage> { DefaultGpxStorage() }
    single<GpxMetadataStore> { DefaultGpxMetadataStore() }
    single<GpxRepository> { DefaultGpxRepository(get(), get()) }
    single<DestinationRepository> { DefaultDestinationRepository() }
    single {
        get<RoomDatabase.Builder<HukiDatabase>>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<HukiDatabase>().placeHistoryDao() }
    single<PlaceHistoryRepository> { DefaultPlaceHistoryRepository(get(), get()) }
    single<Clock> { Clock.System }
    single<CoroutineDispatcher>(named(Dispatcher.Default)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(Dispatcher.IO)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(Dispatcher.Main)) { Dispatchers.Main }
}

val viewModelModule = module {
    viewModel {
        MainViewModel(get(), get(), get(), get(), get(), get(named(Dispatcher.Default)))
    }
    viewModel {
        PlaceFinderViewModel(get(), get(), get(), get(), get(), get(named(Dispatcher.Default)))
    }
    viewModelOf(::MenuViewModel)
    viewModelOf(::GpxCollectionViewModel)
    viewModelOf(::PlaceHistoryViewModel)
    viewModel {
        DestinationsViewModel(get(), get(), get(), get(named(Dispatcher.Default)))
    }
}

val networkModule = module {
    single<HttpClient> { createHttpClient(get()) }
    single<GeocodingRepository> { LocationIqGeocodingRepository(get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    Logger.setLogWriters(platformLogWriter())
    Logger.setTag("HuKi")

    startKoin {
        config?.invoke(this)
        modules(appModule, viewModelModule, networkModule)
    }
}
