package hu.mostoha.mobile.kmp.huki.features.menu

import hu.mostoha.mobile.kmp.huki.WhatsNewContent

data class MenuUiState(val versionName: String = WhatsNewContent.currentVersion) {
    companion object {
        val Default = MenuUiState()
    }
}
