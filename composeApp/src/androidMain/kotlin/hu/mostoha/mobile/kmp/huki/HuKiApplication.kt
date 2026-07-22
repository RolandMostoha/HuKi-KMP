package hu.mostoha.mobile.kmp.huki

import android.app.Application
import hu.mostoha.mobile.kmp.huki.analytics.AndroidAnalyticsService
import hu.mostoha.mobile.kmp.huki.analytics.AndroidCrashlyticsService
import hu.mostoha.mobile.kmp.huki.di.androidPlatformModule
import hu.mostoha.mobile.kmp.huki.di.initKoin
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsDecoratorService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class HuKiApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@HuKiApplication)
            modules(
                androidPlatformModule,
                module { single<CrashlyticsService> { AndroidCrashlyticsService() } },
                module {
                    single<AnalyticsService> {
                        CrashlyticsDecoratorService(AndroidAnalyticsService(), get())
                    }
                },
            )
        }
    }
}
