package hu.mostoha.mobile.kmp.huki.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphhopperRouteRequest(
    @SerialName("profile")
    val profile: GraphhopperProfile,
    @SerialName("points")
    val points: List<List<Double>>,
    @SerialName("points_encoded")
    val pointsEncoded: Boolean,
    @SerialName("elevation")
    val elevation: Boolean,
    @SerialName("instructions")
    val instructions: Boolean,
    @SerialName("custom_model")
    val customModel: GraphhopperCustomModel? = null,
    @SerialName("ch.disable")
    val chDisabled: Boolean? = null,
    @SerialName("algorithm")
    val algorithm: GraphhopperAlgorithm? = null,
    @SerialName("round_trip.distance")
    val roundTripDistance: Int? = null,
    @SerialName("round_trip.seed")
    val roundTripSeed: Int? = null,
    @SerialName("headings")
    val headings: List<Int>? = null,
    @SerialName("heading_penalty")
    val headingPenalty: Int? = null,
)
