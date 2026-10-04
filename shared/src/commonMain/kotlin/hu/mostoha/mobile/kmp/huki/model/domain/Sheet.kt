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
     * Place Details Standard Sheet is shown.
     */
    data object PlaceDetails : Sheet()

    /**
     * Route Planner Standard Sheet is shown.
     */
    data class RoutePlanner(val place: Place?) : Sheet()

    /**
     * WhatsNew Modal Sheet is shown after the app is updated.
     */
    data class WhatsNew(val whatsNew: WhatsNewModel) : Sheet()

    /**
     * Discover Modal Sheet is shown.
     */
    data object Discover : Sheet()

    /**
     * OKT Standard Sheet is shown with the sections of the Blue Trail.
     */
    data object Okt : Sheet()
}

fun Sheet.isStandard(): Boolean =
    this is Sheet.Gpx ||
        this is Sheet.Search ||
        this is Sheet.PlaceDetails ||
        this is Sheet.RoutePlanner ||
        this is Sheet.Okt

fun Sheet.isModal(): Boolean = this is Sheet.Layers || this is Sheet.WhatsNew || this is Sheet.Discover
