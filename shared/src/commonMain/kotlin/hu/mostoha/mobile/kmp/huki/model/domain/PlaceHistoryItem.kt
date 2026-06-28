package hu.mostoha.mobile.kmp.huki.model.domain

import kotlin.time.Instant

data class PlaceHistoryItem(
    val place: Place,
    val lastVisited: Instant,
)
