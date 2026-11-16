package hu.mostoha.mobile.kmp.huki.features.menu

sealed interface MenuUiEvents {
    data object ScreenViewed : MenuUiEvents
    data object BackClicked : MenuUiEvents
    data object SettingsClicked : MenuUiEvents
    data object RoutePlannerClicked : MenuUiEvents
    data object DiscoverClicked : MenuUiEvents
    data object PlaceHistoryClicked : MenuUiEvents
    data object GpxCollectionClicked : MenuUiEvents
    data object GpxGuideClicked : MenuUiEvents
    data object TrailSymbolsGuideClicked : MenuUiEvents
    data object QuickGuideClicked : MenuUiEvents
    data object EmailClicked : MenuUiEvents
    data object LocationIqClicked : MenuUiEvents

    sealed interface WebsiteClicked : MenuUiEvents
    data object PrivacyPolicyClicked : WebsiteClicked
    data object FacebookClicked : WebsiteClicked
    data object GithubClicked : WebsiteClicked
    data object GraphhopperClicked : WebsiteClicked
    data object TuHuClicked : WebsiteClicked
    data object WaymarkedTrailsClicked : WebsiteClicked
    data object SupportClicked : MenuUiEvents
}
