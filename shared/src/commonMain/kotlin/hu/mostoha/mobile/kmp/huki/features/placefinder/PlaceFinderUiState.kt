package hu.mostoha.mobile.kmp.huki.features.placefinder

import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.Place

data class PlaceFinderUiState(
    val searchText: String = "",
    val isLoading: Boolean = false,
    val places: List<Place> = emptyList(),
    val error: InfoViewData? = null,
    val topDestinations: List<Destination> = emptyList(),
    val recentGpxFiles: List<GpxFileItem> = emptyList(),
) {
    companion object {
        val Default = PlaceFinderUiState()
    }
}
