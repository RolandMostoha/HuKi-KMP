package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.model.domain.WhatsNew as WhatsNewModel

sealed class Sheet {
    /**
     * Search is expanded to a full-screen Standard Sheet.
     */
    data object Search : Sheet()

    /**
     * Layers Modal Sheet is shown.
     */
    data object Layers : Sheet()

    /**
     * GPX Details Standard Sheet is shown.
     */
    data class Gpx(val gpxDetails: GpxDetails) : Sheet()

    /**
     * WhatsNew Modal Sheet is shown after the app is updated.
     */
    data class WhatsNew(val whatsNew: WhatsNewModel) : Sheet()
}

fun Sheet.isStandard(): Boolean = this is Sheet.Gpx || this is Sheet.Search

fun Sheet.isModal(): Boolean = this is Sheet.Layers || this is Sheet.WhatsNew
