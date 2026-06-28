package hu.mostoha.mobile.kmp.huki.model.domain

data class DestinationListItem(
    val destination: Destination,
    val landscape: Landscape? = null,
    val distance: String? = null,
)
