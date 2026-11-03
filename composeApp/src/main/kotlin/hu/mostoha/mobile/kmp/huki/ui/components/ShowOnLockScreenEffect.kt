package hu.mostoha.mobile.kmp.huki.ui.components

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

@Composable
fun ShowOnLockScreenEffect(settingsRepository: SettingsRepository = koinInject()) {
    val activity = LocalActivity.current ?: return
    val enabledFlow = remember(settingsRepository) {
        settingsRepository.settings.map { it.showOnLockScreen }.distinctUntilChanged()
    }
    val enabled by enabledFlow.collectAsStateWithLifecycle(initialValue = false)
    DisposableEffect(activity, enabled) {
        activity.setShowOnLockScreen(enabled)
        onDispose { activity.setShowOnLockScreen(false) }
    }
}

private fun Activity.setShowOnLockScreen(show: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(show)
    } else if (show) {
        @Suppress("DEPRECATION")
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    } else {
        @Suppress("DEPRECATION")
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }
}
