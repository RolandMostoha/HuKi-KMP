package hu.mostoha.mobile.kmp.huki.features.placehistory

import hu.mostoha.mobile.kmp.huki.model.domain.Place

sealed interface PlaceHistoryUiEvents {
    data object BackClicked : PlaceHistoryUiEvents
    data class PlaceClicked(val place: Place) : PlaceHistoryUiEvents
}
