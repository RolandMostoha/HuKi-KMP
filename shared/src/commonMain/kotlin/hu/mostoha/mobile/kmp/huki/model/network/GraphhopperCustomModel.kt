package hu.mostoha.mobile.kmp.huki.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphhopperCustomModel(
    @SerialName("priority")
    val priority: List<GraphhopperPriority>,
)

@Serializable
data class GraphhopperPriority(
    @SerialName("if")
    val ifCondition: String,
    @SerialName("multiply_by")
    val multiplyBy: String,
)
