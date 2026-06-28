package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.data.LandscapeByDestinationId
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationListItem

fun Destination.toDestinationListItem(distance: String? = null): DestinationListItem =
    DestinationListItem(
        destination = this,
        landscape = LandscapeByDestinationId[osmId],
        distance = distance,
    )
