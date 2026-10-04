package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.desc.StringDesc

data class OktInfoWindowData(
    val marker: OktMarker,
    val title: StringDesc,
    val distance: String? = null,
    val travelTime: StringDesc? = null,
)
