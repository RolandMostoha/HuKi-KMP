package hu.mostoha.mobile.kmp.huki.features.destinations

sealed interface DestinationsUiEvents {
    data object BackClicked : DestinationsUiEvents
    data class TabSelected(val tab: DestinationsTab) : DestinationsUiEvents
    data object GrantLocationClicked : DestinationsUiEvents
    data object RetryNearbyClicked : DestinationsUiEvents
}
