package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarker
import hu.mostoha.mobile.kmp.huki.model.domain.OktType

sealed interface OktUiEvents : MainUiEvents {
    data class OktTypeClicked(val type: OktType) : OktUiEvents
    data object OktInfoClicked : OktUiEvents
    data class OktSectionClicked(val sectionId: String) : OktUiEvents
    data class OktSectionStartClicked(val sectionId: String) : OktUiEvents
    data class OktSectionWebsiteClicked(val sectionId: String) : OktUiEvents
    data class OktSectionReverseClicked(val sectionId: String) : OktUiEvents
    data object OktResumeClicked : OktUiEvents
    data class OktMarkerClicked(val marker: OktMarker) : OktUiEvents
    data class OktLineClicked(val location: Location) : OktUiEvents
    data object OktInfoWindowDismissed : OktUiEvents
    data object OktInfoWindowPlaceDetailsClicked : OktUiEvents
    data object OktCloseClicked : OktUiEvents
}
