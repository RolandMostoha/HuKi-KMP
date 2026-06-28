package hu.mostoha.mobile.kmp.huki.model.domain

sealed interface PlaceHistoryHeader {
    data object Today : PlaceHistoryHeader
    data object Yesterday : PlaceHistoryHeader
    data class Date(val label: String) : PlaceHistoryHeader
}
