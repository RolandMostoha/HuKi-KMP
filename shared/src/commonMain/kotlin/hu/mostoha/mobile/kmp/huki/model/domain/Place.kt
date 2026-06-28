package hu.mostoha.mobile.kmp.huki.model.domain

data class Place(
    val osmId: String,
    val location: Location,
    val name: String,
    val placeSource: PlaceSource,
    val address: String? = null,
    val placeCategory: PlaceCategory? = null,
    val osmType: OsmType? = null,
    val distance: String? = null,
    val boundingBox: BoundingBox? = null,
)
