package hu.mostoha.mobile.kmp.huki.di

import dev.icerock.moko.permissions.PermissionsController
import org.koin.dsl.module

val androidPlatformModule = module {
    single { PermissionsController(applicationContext = get()) }
}
