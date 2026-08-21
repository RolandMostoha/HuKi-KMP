package hu.mostoha.mobile.kmp.huki.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphhopperRouteResponse(
    @SerialName("paths")
    val paths: List<GraphhopperPath> = emptyList(),
)

@Serializable
data class GraphhopperPath(
    @SerialName("distance")
    val distance: Double,
    @SerialName("time")
    val time: Long,
    @SerialName("ascend")
    val ascend: Double = 0.0,
    @SerialName("descend")
    val descend: Double = 0.0,
    @SerialName("points")
    val points: GraphhopperPoints,
    @SerialName("snapped_waypoints")
    val snappedWaypoints: GraphhopperPoints,
)

@Serializable
data class GraphhopperPoints(
    @SerialName("coordinates")
    val coordinates: List<List<Double>> = emptyList(),
)
