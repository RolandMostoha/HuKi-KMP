package hu.mostoha.mobile.kmp.huki.features.settings

sealed interface SettingsUiEvents {
    data object ScreenViewed : SettingsUiEvents
    data object BackClicked : SettingsUiEvents
    data class MapZoomControlsToggled(val visible: Boolean) : SettingsUiEvents
}
