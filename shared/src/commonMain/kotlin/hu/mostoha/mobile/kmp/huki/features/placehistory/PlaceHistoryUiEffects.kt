package hu.mostoha.mobile.kmp.huki.features.placehistory

import hu.mostoha.mobile.kmp.huki.features.main.UiEffect
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType

sealed interface PlaceHistoryUiEffects : UiEffect {
    data object NavigateBack : PlaceHistoryUiEffects
    data class OpenPlace(
        val osmType: OsmType,
        val osmId: String,
    ) : PlaceHistoryUiEffects
}
