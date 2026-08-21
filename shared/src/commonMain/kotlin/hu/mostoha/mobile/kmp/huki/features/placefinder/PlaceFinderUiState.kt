package hu.mostoha.mobile.kmp.huki.features.placefinder

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.Place

data class PlaceFinderUiState(
    val searchText: String = "",
    val isLoading: Boolean = false,
    val places: List<Place> = emptyList(),
    val searchRecentPlaces: List<Place> = emptyList(),
    val searchDestinations: List<Destination> = emptyList(),
    val error: InfoViewData? = null,
    val destinations: List<Destination> = emptyList(),
    val destinationsTitle: StringResource = SharedRes.strings.destinations_section_title,
    val recentPlaces: List<Place> = emptyList(),
    val recentGpxFiles: List<GpxFileItem> = emptyList(),
) {
    companion object {
        val Default = PlaceFinderUiState()
    }
}
