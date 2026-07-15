package hu.mostoha.mobile.kmp.huki.features.settings

import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences

data class SettingsUiState(val mapZoomControlsVisible: Boolean = UserPreferences.DEFAULTS.mapZoomControlsVisible) {
    companion object {
        val Default = SettingsUiState()
    }
}
