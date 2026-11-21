package hu.mostoha.mobile.kmp.huki

import android.app.Application
import androidx.compose.ui.AndroidComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mapbox.common.HttpServiceFactory
import com.mapbox.common.MapboxOptions
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.kmp.huki.analytics.AndroidAnalyticsService
import hu.mostoha.mobile.kmp.huki.analytics.AndroidCrashlyticsService
import hu.mostoha.mobile.kmp.huki.billing.PlayBillingService
import hu.mostoha.mobile.kmp.huki.di.androidPlatformModule
import hu.mostoha.mobile.kmp.huki.di.initKoin
import hu.mostoha.mobile.kmp.huki.migration.LegacyCleanup
import hu.mostoha.mobile.kmp.huki.repository.SupporterRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.BillingService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsDecoratorService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.util.OverlayTileCacheInterceptor
import hu.mostoha.mobile.kmp.huki.util.setDebugBuild
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class HuKiApplication : Application() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate() {
        super.onCreate()

        // Compose UI 1.12 runs a text field's stopInput before the next field's startInput, which hides and
        // reshows the keyboard on every focus move between fields (b/513525072).
        AndroidComposeUiFlags.isOutOfFrameSchedulerForTextInputEventsEnabled = false
        setDebugBuild(BuildConfig.DEBUG)
        MapboxOptions.accessToken = Secrets.MAPBOX_ACCESS_TOKEN
        HttpServiceFactory.setHttpServiceInterceptor(OverlayTileCacheInterceptor())

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
                module { single<BillingService> { PlayBillingService(androidApplication()) } },
            )
        }
        refreshSupporterStateInForeground()
        cleanUpLegacyApp()
    }

    private fun cleanUpLegacyApp() {
        val legacyCleanup = get<LegacyCleanup>()
        ProcessLifecycleOwner.get().lifecycleScope.launch { legacyCleanup.run() }
    }

    /**
     * Picks up purchases completed while the app was in the background, e.g. an approved pending payment.
     */
    private fun refreshSupporterStateInForeground() {
        val supporterRepository = get<SupporterRepository>()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    owner.lifecycleScope.launch { supporterRepository.refresh() }
                }
            },
        )
    }
}
