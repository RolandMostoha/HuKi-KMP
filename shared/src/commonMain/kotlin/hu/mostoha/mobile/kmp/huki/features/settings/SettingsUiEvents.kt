package hu.mostoha.mobile.kmp.huki.features.settings

sealed interface SettingsUiEvents {
    data object BackClicked : SettingsUiEvents
    data object EmailClicked : SettingsUiEvents
    data object FacebookClicked : SettingsUiEvents
    data object GithubClicked : SettingsUiEvents
    data object LocationIqClicked : SettingsUiEvents
}
