package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqAddress
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter

const val LOCATION_IQ_BB_SIZE = 4
const val LOCATION_IQ_BB_NORTH_INDEX = 1
const val LOCATION_IQ_BB_EAST_INDEX = 3
const val LOCATION_IQ_BB_SOUTH_INDEX = 0
const val LOCATION_IQ_BB_WEST_INDEX = 2

fun LocationIqPlace.toReverseGeocodedPlace(location: Location, userLocation: Location? = null): Place =
    Place(
        osmId = osmId,
        name = displayPlace ?: address?.toPlaceName() ?: displayName,
        placeSource = PlaceSource.LONG_TAP_ON_MAP,
        address = displayAddress ?: address?.toAddressLine() ?: displayName,
        location = location,
        placeCategory = PlaceCategory.fromString(osmTag = type, osmClass = classType),
        osmType = OsmType.fromString(osmType),
        distance = userLocation?.let {
            DistanceFormatter.formatDistance(it.distanceBetween(location))
        },
    )

fun Place.withDistanceFrom(userLocation: Location?): Place =
    if (distance != null || userLocation == null) {
        this
    } else {
        copy(distance = DistanceFormatter.formatDistance(userLocation.distanceBetween(location)))
    }

private fun LocationIqAddress.toPlaceName(): String? =
    name ?: listOfNotNull(road, houseNumber)
        .takeIf { it.isNotEmpty() }
        ?.joinToString(" ")

private fun LocationIqAddress.toAddressLine(): String? =
    listOfNotNull(postcode, city, county)
        .distinct()
        .takeIf { it.isNotEmpty() }
        ?.joinToString(", ")

fun LocationIqPlace.toPlaceSearchResult(userLocation: Location? = null): Place {
    val location = Location(lat, lon)
    return Place(
        osmId = osmId,
        name = displayPlace ?: displayName,
        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        address = displayAddress,
        location = location,
        placeCategory = PlaceCategory.fromString(osmTag = type, osmClass = classType),
        osmType = OsmType.fromString(osmType),
        distance = userLocation?.let {
            DistanceFormatter.formatDistance(it.distanceBetween(location))
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
