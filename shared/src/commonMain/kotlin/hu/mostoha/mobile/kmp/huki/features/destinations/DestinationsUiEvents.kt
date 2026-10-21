package hu.mostoha.mobile.kmp.huki.features.destinations

import hu.mostoha.mobile.kmp.huki.model.domain.Destination

sealed interface DestinationsUiEvents {
    data object ScreenViewed : DestinationsUiEvents
    data object BackClicked : DestinationsUiEvents
    data class TabSelected(val tab: DestinationsTab) : DestinationsUiEvents
    data object GrantLocationClicked : DestinationsUiEvents
    data object RetryNearbyClicked : DestinationsUiEvents
    data class WebsiteClicked(val destination: Destination) : DestinationsUiEvents
}
