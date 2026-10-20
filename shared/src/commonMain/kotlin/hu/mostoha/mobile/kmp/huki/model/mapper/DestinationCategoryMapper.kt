package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationCategory
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationChallenge
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType

fun DestinationType.toDestinationCategory(destinations: List<Destination>): DestinationCategory =
    DestinationCategory(
        id = name.lowercase(),
        title = title,
        iconRes = iconRes,
        colorRes = colorRes,
        destinations = destinations,
    )

fun DestinationChallenge.toDestinationCategory(destinations: List<Destination>): DestinationCategory =
    DestinationCategory(
        id = name.lowercase(),
        title = title,
        iconRes = iconRes,
        colorRes = placeCategory.categoryColorRes,
        destinations = destinations,
    )
