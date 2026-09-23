package hu.mostoha.mobile.kmp.huki.di

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class IosViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()

    fun clear() {
        viewModelStore.clear()
    }
}
