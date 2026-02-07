package hu.mostoha.mobile.kmp.huki.model.domain

data class CameraPosition(
    val location: Location,
    val zoom: Double,
    val bearing: Double,
    val pitch: Double,
)
