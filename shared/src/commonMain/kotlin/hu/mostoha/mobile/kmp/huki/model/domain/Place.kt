package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace

data class Place(
    val id: String,
    val title: String,
    val subtitle: String?,
    val location: Location,
)

fun LocationIqPlace.toPlaceSearchResult(): Place =
    Place(
        id = placeId,
        title = displayPlace ?: displayName,
        subtitle = displayAddress,
        location = Location(lat, lon),
    )
