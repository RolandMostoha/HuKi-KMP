package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.data.LandscapeByDestinationId
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationListItem
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource

fun Destination.toDestinationListItem(distance: String? = null): DestinationListItem =
    DestinationListItem(
        destination = this,
        landscape = LandscapeByDestinationId[osmId],
        distance = distance,
    )

fun Destination.toPlace(): Place =
    Place(
        osmId = osmId,
        location = location,
        name = name,
        placeSource = PlaceSource.DESTINATIONS,
        address = town,
        placeCategory = type.placeCategory,
        osmType = OsmType.NODE,
    )
