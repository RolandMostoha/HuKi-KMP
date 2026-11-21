package hu.mostoha.mobile.kmp.huki.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GraphhopperAlgorithm {
    @SerialName("round_trip")
    ROUND_TRIP,
}
