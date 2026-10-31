package hu.mostoha.mobile.kmp.huki.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Holds only references and flags, the GPX and trail geometry is re-read on restore to keep the saved state small.
 */
@Serializable
data class MainSessionSnapshot(
    val version: Int = VERSION,
    val camera: CameraSnapshot,
    val myLocationStatus: String,
    val hikingLayerVisible: Boolean,
    val feature: FeatureSnapshot? = null,
) {
    companion object {
        const val VERSION = 1
    }
}

@Serializable
data class CameraSnapshot(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double,
    val bearing: Double,
    val pitch: Double,
    val padding: String? = null,
)

@Serializable
sealed interface FeatureSnapshot {
    @Serializable
    @SerialName("gpx")
    data class Gpx(
        val fileUri: String,
        val isReversed: Boolean,
        val isLayerVisible: Boolean,
        val isRouteVisible: Boolean,
        val isAllDistancesVisible: Boolean,
        val isSlopeColorsVisible: Boolean,
        val isSheetVisible: Boolean,
    ) : FeatureSnapshot

    @Serializable
    @SerialName("okt")
    data class Okt(
        val oktType: String,
        val selectedSectionId: String,
        val reversedSectionIds: Set<String>,
        val isSheetVisible: Boolean,
    ) : FeatureSnapshot

    @Serializable
    @SerialName("landscapes")
    data class Landscapes(
        val grouping: String,
        val selectedLandscapeId: String? = null,
        val selectedCategoryId: String? = null,
        val selectedDestinationId: String? = null,
        val isSheetVisible: Boolean,
    ) : FeatureSnapshot

    @Serializable
    @SerialName("route_planner")
    data class RoutePlanner(
        val routeProfile: String,
        val waypoints: List<WaypointSnapshot>,
    ) : FeatureSnapshot
}

/**
 * @property isPlaceName the stop is named by [placeName], otherwise the name is derived (My location, coordinates).
 */
@Serializable
data class WaypointSnapshot(
    val id: String,
    val isPlaceName: Boolean = false,
    val isMyLocation: Boolean = false,
    val placeName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
