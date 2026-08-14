package hu.mostoha.mobile.kmp.huki.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GraphhopperProfile {
    @SerialName("hike")
    HIKE,

    @SerialName("bike")
    BIKE,
}
