package hu.mostoha.mobile.kmp.huki

import android.app.Application
import hu.mostoha.mobile.kmp.huki.analytics.AndroidAnalyticsService
import hu.mostoha.mobile.kmp.huki.di.androidPlatformModule
import hu.mostoha.mobile.kmp.huki.di.initKoin
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class HuKiApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@HuKiApplication)
            modules(
                androidPlatformModule,
                module { single<AnalyticsService> { AndroidAnalyticsService() } },
            )
        }
    }
}
