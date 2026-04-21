package hu.mostoha.mobile.kmp.huki.di

import dev.icerock.moko.permissions.PermissionsController
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

val androidPlatformModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { PermissionsController(applicationContext = get()) }
}
