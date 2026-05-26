package hu.mostoha.mobile.kmp.huki.di

import dev.icerock.moko.permissions.ios.PermissionsController
import dev.icerock.moko.permissions.ios.PermissionsControllerProtocol
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import hu.mostoha.mobile.kmp.huki.features.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.kmp.huki.features.settings.SettingsViewModel
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module

val iosPlatformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<PermissionsControllerProtocol> { PermissionsController() }
}

fun initKoin() {
    initKoin {
        modules(iosPlatformModule)
    }
}

object KoinViewModelProvider : KoinComponent {
    fun getMainViewModel(): MainViewModel = get()
    fun getPlaceFinderViewModel(): PlaceFinderViewModel = get()
    fun getSettingsViewModel(): SettingsViewModel = get()
}
