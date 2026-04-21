package hu.mostoha.mobile.kmp.huki.features.placefinder

import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.Place

data class PlaceFinderUiState(
    val searchText: String = "",
    val isLoading: Boolean = false,
    val places: List<Place> = emptyList(),
    val error: InfoViewData? = null,
) {
    companion object {
        val Default = PlaceFinderUiState()
    }
}
