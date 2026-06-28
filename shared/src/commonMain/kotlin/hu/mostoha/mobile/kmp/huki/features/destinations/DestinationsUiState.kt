package hu.mostoha.mobile.kmp.huki.features.destinations

import hu.mostoha.mobile.kmp.huki.model.domain.DestinationListItem
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.Location

data class DestinationsUiState(
    val isLoading: Boolean = true,
    val selectedTab: DestinationsTab = DestinationsTab.POPULAR,
    val destinationCount: Int = 0,
    val popularItems: List<DestinationListItem> = emptyList(),
    val landscapes: List<Landscape> = emptyList(),
    val nearbyState: NearbyState = NearbyState.Loading,
    val userLocation: Location? = null,
) {
    companion object {
        val Default = DestinationsUiState()
    }
}

sealed interface NearbyState {
    data object Loading : NearbyState
    data object PermissionRequired : NearbyState
    data object LocationUnavailable : NearbyState
    data class Loaded(val items: List<DestinationListItem>) : NearbyState
}
