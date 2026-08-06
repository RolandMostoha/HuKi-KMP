package hu.mostoha.mobile.kmp.huki.model.domain

sealed class PlaceDetails {
    abstract val location: Location

    data class Loading(override val location: Location) : PlaceDetails()

    data class Unresolved(
        override val location: Location,
        val distance: String? = null,
    ) : PlaceDetails()

    data class PlaceLoaded(val place: Place) : PlaceDetails() {
        override val location: Location get() = place.location
    }
}
