package hu.mostoha.mobile.kmp.huki.features.menu

sealed interface MenuUiEvents {
    data object BackClicked : MenuUiEvents
    data object SettingsClicked : MenuUiEvents
    data object DestinationsClicked : MenuUiEvents
    data object PlaceHistoryClicked : MenuUiEvents
    data object GpxCollectionClicked : MenuUiEvents
    data object GpxGuideClicked : MenuUiEvents
    data object TrailSymbolsGuideClicked : MenuUiEvents
    data object EmailClicked : MenuUiEvents
    data object PrivacyPolicyClicked : MenuUiEvents
    data object FacebookClicked : MenuUiEvents
    data object GithubClicked : MenuUiEvents
    data object LocationIqClicked : MenuUiEvents
}
