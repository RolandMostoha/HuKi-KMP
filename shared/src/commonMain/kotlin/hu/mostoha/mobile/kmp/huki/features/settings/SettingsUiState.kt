package hu.mostoha.mobile.kmp.huki.features.settings

import hu.mostoha.mobile.kmp.huki.model.domain.MapLabelScale
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences

data class SettingsUiState(
    val mapZoomControlsVisible: Boolean = UserPreferences.DEFAULTS.mapZoomControlsVisible,
    val mapRotationEnabled: Boolean = UserPreferences.DEFAULTS.mapRotationEnabled,
    val mapLabelScale: MapLabelScale = UserPreferences.DEFAULTS.mapLabelScale,
    val themeMode: ThemeMode = UserPreferences.DEFAULTS.themeMode,
    val gpxDistancesVisible: Boolean = UserPreferences.DEFAULTS.gpxDistancesVisible,
    val gpxSlopeColorsVisible: Boolean = UserPreferences.DEFAULTS.gpxSlopeColorsVisible,
) {
    companion object {
        val Default = SettingsUiState()
    }
}
