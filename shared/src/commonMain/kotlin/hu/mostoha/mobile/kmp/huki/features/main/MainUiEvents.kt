package hu.mostoha.mobile.kmp.huki.features.main

sealed interface MainUiEvents {
    data object MyLocationClicked : MainUiEvents
    data object FollowingDisabled : MainUiEvents
}
