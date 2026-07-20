package hu.mostoha.mobile.kmp.huki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import hu.mostoha.mobile.kmp.huki.navigation.RootNavHost
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.AppLaunchConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppLaunchConfig.skipWhatsNew = intent.getBooleanExtra(AppLaunchConfig.ARG_SKIP_WHATS_NEW, false)

        setContent {
            HuKiTheme {
                Surface(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                    RootNavHost()
                }
            }
        }
    }
}
