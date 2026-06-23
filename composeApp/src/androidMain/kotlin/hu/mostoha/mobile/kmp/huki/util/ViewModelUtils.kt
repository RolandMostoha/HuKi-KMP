package hu.mostoha.mobile.kmp.huki.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Scopes ViewModels to the calling Composable instead of the host Activity/back stack entry, so the
 * store is cleared when the Composable leaves composition. Use it (via LocalViewModelStoreOwner).
 *
 * Note: the store does not survive configuration changes. Replace with `rememberViewModelStoreOwner`
 * once the project is on AGP 9.1 / compileSdk 37 (androidx.lifecycle 2.11.0).
 */
@Composable
fun rememberScopedViewModelStoreOwner(): ViewModelStoreOwner {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(Unit) {
        onDispose { owner.viewModelStore.clear() }
    }
    return owner
}
