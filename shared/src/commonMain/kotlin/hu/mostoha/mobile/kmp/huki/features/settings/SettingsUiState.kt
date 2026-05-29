package hu.mostoha.mobile.kmp.huki.features.settings

data class SettingsUiState(val versionName: String = "0.9.0") {
    companion object {
        val Default = SettingsUiState()
    }
}
