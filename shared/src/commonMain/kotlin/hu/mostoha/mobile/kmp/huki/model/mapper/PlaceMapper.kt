package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter

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
            DistanceFormatter.formatRoundedDistance(it.distanceBetween(location))
        },
    )
}
