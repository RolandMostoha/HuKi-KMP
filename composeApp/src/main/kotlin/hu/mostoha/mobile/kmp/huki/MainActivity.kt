package hu.mostoha.mobile.kmp.huki

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
import hu.mostoha.mobile.kmp.huki.navigation.RootNavHost
import hu.mostoha.mobile.kmp.huki.repository.IncomingLinkStore
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.AppLaunchConfig
import hu.mostoha.mobile.kmp.huki.util.copyTestGpxToCache
import hu.mostoha.mobile.kmp.huki.util.isDebugBuild
import hu.mostoha.mobile.kmp.huki.util.toIncomingLink
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    private val incomingLinkStore: IncomingLinkStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppLaunchConfig.skipWhatsNew = intent.getBooleanExtra(AppLaunchConfig.ARG_SKIP_WHATS_NEW, false)
        if (isDebugBuild && intent.getBooleanExtra(AppLaunchConfig.ARG_IMPORT_GPX, false)) {
            AppLaunchConfig.importGpxPath = copyTestGpxToCache()
        }
        // A recreated activity still holds its launch intent, which was already opened
        if (savedInstanceState == null) openIncomingLink(intent)

        setContent {
            val settingsRepository = koinInject<SettingsRepository>()
            val themeModes = remember(settingsRepository) { settingsRepository.settings.map { it.themeMode } }
            val themeMode by themeModes.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            HuKiTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                    RootNavHost()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openIncomingLink(intent)
    }

    private fun openIncomingLink(intent: Intent) {
        intent.toIncomingLink()?.let(incomingLinkStore::push)
    }
}
