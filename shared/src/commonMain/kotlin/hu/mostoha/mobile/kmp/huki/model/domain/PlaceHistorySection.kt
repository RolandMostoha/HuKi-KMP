package hu.mostoha.mobile.kmp.huki.model.domain

data class PlaceHistorySection(
    val header: PlaceHistoryHeader,
    val items: List<PlaceHistoryItem>,
)
