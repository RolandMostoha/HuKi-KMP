package hu.mostoha.mobile.kmp.huki

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
import hu.mostoha.mobile.kmp.huki.navigation.RootNavHost
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.AppLaunchConfig
import hu.mostoha.mobile.kmp.huki.util.copyTestGpxToCache
import hu.mostoha.mobile.kmp.huki.util.isDebugBuild
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppLaunchConfig.skipWhatsNew = intent.getBooleanExtra(AppLaunchConfig.ARG_SKIP_WHATS_NEW, false)
        if (isDebugBuild && intent.getBooleanExtra(AppLaunchConfig.ARG_IMPORT_GPX, false)) {
            AppLaunchConfig.importGpxPath = copyTestGpxToCache()
        }

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
}
