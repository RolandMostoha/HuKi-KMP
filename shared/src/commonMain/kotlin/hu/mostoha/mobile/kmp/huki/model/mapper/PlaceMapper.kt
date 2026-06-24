package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter

const val LOCATION_IQ_BB_SIZE = 4
const val LOCATION_IQ_BB_NORTH_INDEX = 1
const val LOCATION_IQ_BB_EAST_INDEX = 3
const val LOCATION_IQ_BB_SOUTH_INDEX = 0
const val LOCATION_IQ_BB_WEST_INDEX = 2

fun LocationIqPlace.toPlaceSearchResult(userLocation: Location? = null): Place {
    val location = Location(lat, lon)
    return Place(
        osmId = osmId,
        name = displayPlace ?: displayName,
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = displayAddress,
        location = location,
        placeCategory = PlaceCategory.fromString(type),
        osmType = OsmType.fromString(osmType),
        distance = userLocation?.let {
            DistanceFormatter.formatRoundedDistance(it.distanceBetween(location))
        },
        boundingBox = boundingBox?.takeIf { it.size == LOCATION_IQ_BB_SIZE }?.let { doubles ->
            BoundingBox(
                north = doubles[LOCATION_IQ_BB_NORTH_INDEX],
                east = doubles[LOCATION_IQ_BB_EAST_INDEX],
                south = doubles[LOCATION_IQ_BB_SOUTH_INDEX],
                west = doubles[LOCATION_IQ_BB_WEST_INDEX],
            )
        },
    )
}
