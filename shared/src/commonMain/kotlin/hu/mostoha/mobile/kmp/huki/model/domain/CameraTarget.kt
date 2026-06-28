package hu.mostoha.mobile.kmp.huki.model.domain

sealed interface CameraTarget {
    data class Center(
        val location: Location,
        val zoom: Double? = null,
    ) : CameraTarget

    data class Bounds(
        val locations: List<Location>,
        val maxZoom: Double? = null,
    ) : CameraTarget
}
