package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.OktInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.OktLine
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarker
import hu.mostoha.mobile.kmp.huki.model.domain.OktSectionItem
import hu.mostoha.mobile.kmp.huki.model.domain.OktType

data class OktUiState(
    val type: OktType,
    val sections: List<OktSectionItem>,
    val selectedSectionId: String,
    val baseLine: OktLine,
    val selectedLine: OktLine,
    val markers: List<OktMarker>,
    val selectedMarkerId: String? = null,
    val infoWindow: OktInfoWindowData? = null,
    val reversedSectionIds: Set<String> = emptySet(),
) {
    val selectedSection: OktSectionItem
        get() = sections.first { it.id == selectedSectionId }

    fun isReversed(sectionId: String): Boolean = sectionId in reversedSectionIds
}
