package hu.mostoha.mobile.kmp.huki.di

import dev.icerock.moko.permissions.ios.PermissionsController
import dev.icerock.moko.permissions.ios.PermissionsControllerProtocol
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module

val iosPlatformModule = module {
    single<PermissionsControllerProtocol> { PermissionsController() }
}

fun initKoin() {
    initKoin {
        modules(iosPlatformModule)
    }
}

object KoinViewModelProvider : KoinComponent {
    fun getMainViewModel(): MainViewModel = get()
}
