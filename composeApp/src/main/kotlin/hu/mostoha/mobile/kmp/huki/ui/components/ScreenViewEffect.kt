package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleStartEffect

@Composable
fun ScreenViewEffect(onScreenView: () -> Unit) {
    val activityLifecycle = (LocalActivity.current as? LifecycleOwner)?.lifecycle
    val currentOnScreenView by rememberUpdatedState(onScreenView)
    // Saveable so a config change (rotation, dark mode, language) doesn't log the screen view again
    var isScreenViewLogged by rememberSaveable { mutableStateOf(false) }
    LifecycleStartEffect(Unit) {
        if (!isScreenViewLogged) {
            isScreenViewLogged = true
            currentOnScreenView()
        }
        onStopOrDispose {
            // Activity still started = navigated away, so returning to this screen logs again
            if (activityLifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) {
                isScreenViewLogged = false
            }
        }
    }
}
