package hu.mostoha.mobile.kmp.huki.features.placefinder

sealed interface PlaceFinderUiEvents {
    data class SearchTextChanged(val searchText: String) : PlaceFinderUiEvents
    data object RetryClicked : PlaceFinderUiEvents
}
