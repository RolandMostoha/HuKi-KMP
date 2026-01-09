package hu.mostoha.mobile.kmp.huki.model.domain

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
)
