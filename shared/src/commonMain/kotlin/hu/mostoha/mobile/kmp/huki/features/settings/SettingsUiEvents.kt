package hu.mostoha.mobile.kmp.huki.features.settings

import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode

sealed interface SettingsUiEvents {
    data object ScreenViewed : SettingsUiEvents
    data object BackClicked : SettingsUiEvents
    data class MapZoomControlsToggled(val visible: Boolean) : SettingsUiEvents
    data class ThemeModeSelected(val themeMode: ThemeMode) : SettingsUiEvents
}
