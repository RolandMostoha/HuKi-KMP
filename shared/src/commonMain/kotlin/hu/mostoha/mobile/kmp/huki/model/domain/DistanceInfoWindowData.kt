package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.desc.StringDesc

data class DistanceInfoWindowData(
    val id: Int,
    val location: Location,
    val name: String?,
    val label: StringDesc?,
    val distance: String?,
    val travelTime: StringDesc?,
    val priority: Int,
)
