package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.util.TrackPosition
import hu.mostoha.mobile.kmp.huki.util.TrackProgressIndex

data class OktTrail(
    val type: OktType,
    val locations: List<Location>,
    val sections: List<OktSectionGeometry>,
    val baseLine: List<Location>,
    val progressIndex: TrackProgressIndex,
    val markerPositions: Map<String, TrackPosition>,
) {
    override fun toString(): String =
        "OktTrail(type=$type, locations=${locations.size}, sections=${sections.size}, baseLine=${baseLine.size})"
}

data class OktSectionGeometry(
    val section: OktSection,
    val locations: List<Location>,
    val stamps: List<OktStamp>,
) {
    override fun toString(): String =
        "OktSectionGeometry(section=${section.id}, locations=${locations.size}, stamps=${stamps.size})"
}
