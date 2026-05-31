package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace

data class Place(
    val id: String,
    val location: Location,
    val title: String,
    val subtitle: String? = null,
    val placeCategory: PlaceCategory? = null,
    val osmType: OsmType? = null,
)

fun LocationIqPlace.toPlaceSearchResult(): Place =
    Place(
        id = placeId,
        title = displayPlace ?: displayName,
        subtitle = displayAddress,
        location = Location(lat, lon),
        placeCategory = PlaceCategory.fromString(type),
        osmType = OsmType.fromString(osmType),
    )
