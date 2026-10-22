package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationCategory
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.util.sortedAlongPath

enum class LandscapesGrouping {
    LANDSCAPE,
    CATEGORY,
}

data class LandscapesUiState(
    val landscapes: List<Landscape>,
    val outlinesGeoJson: String,
    val labelsGeoJson: String,
    val hungaryMaskGeoJson: String,
    val grouping: LandscapesGrouping = LandscapesGrouping.LANDSCAPE,
    val categories: List<DestinationCategory> = emptyList(),
    val selectedLandscapeId: String? = null,
    val selectedCategoryId: String? = null,
    val selectedDestinationId: String? = null,
) {
    val selectedLandscape: Landscape?
        get() = landscapes.firstOrNull { it.id == selectedLandscapeId }

    val selectedCategory: DestinationCategory?
        get() = categories.firstOrNull { it.id == selectedCategoryId }

    /**
     * Only the selected group reveals its destinations, ordered so that consecutive cards are map neighbours.
     */
    val destinations: List<Destination> by lazy {
        val groupDestinations = when (grouping) {
            LandscapesGrouping.LANDSCAPE -> selectedLandscape?.destinations
            LandscapesGrouping.CATEGORY -> selectedCategory?.destinations
        }
        groupDestinations?.sortedAlongPath { it.location }.orEmpty()
    }

    val selectedDestination: Destination?
        get() = destinations.firstOrNull { it.id == selectedDestinationId }
}
