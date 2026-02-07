package hu.mostoha.mobile.kmp.huki.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import hu.mostoha.mobile.kmp.huki.features.main.MainViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::MainViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    Logger.setLogWriters(platformLogWriter())
    Logger.setTag("HuKi")

    startKoin {
        config?.invoke(this)
        modules(viewModelModule)
    }
}
