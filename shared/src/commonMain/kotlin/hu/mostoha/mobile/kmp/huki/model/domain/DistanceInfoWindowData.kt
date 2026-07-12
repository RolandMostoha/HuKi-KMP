package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.desc.StringDesc

data class DistanceInfoWindowData(
    val location: Location,
    val distance: String,
    val travelTime: StringDesc,
)
