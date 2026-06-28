package hu.mostoha.mobile.kmp.huki.features.placehistory

import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistorySection

data class PlaceHistoryUiState(
    val isLoading: Boolean = true,
    val placeCount: Int = 0,
    val sections: List<PlaceHistorySection> = emptyList(),
) {
    companion object {
        val Default = PlaceHistoryUiState()
    }
}
