package hu.mostoha.mobile.kmp.huki

import android.app.Application
import hu.mostoha.mobile.kmp.huki.di.androidPlatformModule
import hu.mostoha.mobile.kmp.huki.di.initKoin
import org.koin.android.ext.koin.androidContext

class HuKiApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@HuKiApplication)
            modules(androidPlatformModule)
        }
    }
}
