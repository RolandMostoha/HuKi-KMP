package hu.mostoha.mobile.kmp.huki.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import hu.mostoha.mobile.kmp.huki.features.gpxcollection.GpxCollectionViewModel
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.features.menu.MenuViewModel
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.kmp.huki.network.createHttpClient
import hu.mostoha.mobile.kmp.huki.repository.DefaultDestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.DefaultGpxMetadataStore
import hu.mostoha.mobile.kmp.huki.repository.DefaultGpxRepository
import hu.mostoha.mobile.kmp.huki.repository.DefaultGpxStorage
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxMetadataStore
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxStorage
import hu.mostoha.mobile.kmp.huki.repository.LocationIqGeocodingRepository
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<GpxStorage> { DefaultGpxStorage() }
    single<GpxMetadataStore> { DefaultGpxMetadataStore() }
    single<GpxRepository> { DefaultGpxRepository(get(), get()) }
    single<DestinationRepository> { DefaultDestinationRepository() }
}

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::PlaceFinderViewModel)
    viewModelOf(::MenuViewModel)
    viewModelOf(::GpxCollectionViewModel)
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
