package hu.mostoha.mobile.kmp.huki.model.domain

data class Place(
    val id: String,
    val location: Location,
    val title: String,
    val subtitle: String? = null,
    val placeCategory: PlaceCategory? = null,
    val osmType: OsmType? = null,
    val distance: String? = null,
)
