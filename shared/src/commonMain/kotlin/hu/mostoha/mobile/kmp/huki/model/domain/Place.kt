package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter

data class Place(
    val id: String,
    val location: Location,
    val title: String,
    val subtitle: String? = null,
    val placeCategory: PlaceCategory? = null,
    val osmType: OsmType? = null,
    val distance: String? = null,
)

fun LocationIqPlace.toPlaceSearchResult(userLocation: Location? = null): Place {
    val location = Location(lat, lon)
    return Place(
        id = placeId,
        title = displayPlace ?: displayName,
        subtitle = displayAddress,
        location = location,
        placeCategory = PlaceCategory.fromString(type),
        osmType = OsmType.fromString(osmType),
        distance = userLocation?.let {
            DistanceFormatter.formatDistance(it.distanceBetween(location))
        },
    )
}
