package hu.mostoha.mobile.kmp.huki.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class BoundingBox(
    // AKA: maxLat
    val north: Double,
    // AKA: maxLon
    val east: Double,
    // AKA: minLat
    val south: Double,
    // AKA: minLon
    val west: Double,
)

fun BoundingBox.toViewBox(): String = "$east,$north,$west,$south"

fun BoundingBox.center(): Location {
    val centerLat = (south + north) / 2
    val centerLon = (west + east) / 2

    return Location(centerLat, centerLon)
}
