package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperCustomModel
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperPriority
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperProfile
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteRequest
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.util.calculateTravelTime
import hu.mostoha.mobile.kmp.huki.util.formatter.CoordinateFormatter
import org.maplibre.spatialk.units.extensions.meters
import kotlin.math.roundToInt

/**
 * Custom GraphHopper model which prefers trail marks over shortest walking route
 */
private val ON_TRAILS_CUSTOM_MODEL = GraphhopperCustomModel(
    priority = listOf(
        GraphhopperPriority(ifCondition = "foot_network == MISSING", multiplyBy = "0.3"),
    ),
)

private const val LONGITUDE_INDEX = 0
private const val LATITUDE_INDEX = 1
private const val ALTITUDE_INDEX = 2

fun RoutePlannerProfile.toRouteRequest(waypoints: List<Location>): GraphhopperRouteRequest {
    val points = waypoints.map { listOf(it.longitude, it.latitude) }

    return when (this) {
        RoutePlannerProfile.ON_TRAILS -> GraphhopperRouteRequest(
            profile = GraphhopperProfile.HIKE,
            points = points,
            pointsEncoded = false,
            elevation = true,
            instructions = false,
            customModel = ON_TRAILS_CUSTOM_MODEL,
            // A custom model can only be applied when contraction hierarchies are off.
            chDisabled = true,
        )
        RoutePlannerProfile.SHORTEST_ROUTE -> GraphhopperRouteRequest(
            profile = GraphhopperProfile.HIKE,
            points = points,
            pointsEncoded = false,
            elevation = true,
            instructions = false,
        )
        RoutePlannerProfile.BIKE -> GraphhopperRouteRequest(
            profile = GraphhopperProfile.BIKE,
            points = points,
            pointsEncoded = false,
            elevation = true,
            instructions = false,
        )
    }
}

fun Place.toRoutePlannerWaypoint(): RoutePlannerWaypoint =
    RoutePlannerWaypoint(name = StringDesc.Raw(name), placeName = name, location = location)

fun Location.toRoutePlannerWaypoint(): RoutePlannerWaypoint =
    RoutePlannerWaypoint(name = StringDesc.Raw(CoordinateFormatter.formatCoordinates(this)), location = this)

fun Location.toMyLocationWaypoint(): RoutePlannerWaypoint =
    RoutePlannerWaypoint(
        name = StringDesc.Resource(SharedRes.strings.route_planner_my_location),
        location = this,
    )

fun GraphhopperRouteResponse.toRoutePlan(): RoutePlan? {
    val path = paths.firstOrNull() ?: return null
    val locations = path.points.coordinates.toLocations()

    if (locations.isEmpty()) return null

    return RoutePlan(
        waypoints = path.snappedWaypoints.coordinates.toLocations(),
        locations = locations,
        routeStats = RouteStats(
            travelTime = locations.calculateTravelTime(),
            distance = path.distance.meters,
            incline = path.ascend.roundToInt(),
            decline = path.descend.roundToInt(),
        ),
    )
}

private val routePlanDailyLimitInfoViewData = InfoViewData(
    infoViewType = InfoViewType.ERROR,
    title = SharedRes.strings.route_planner_daily_limit_error_title,
    message = SharedRes.strings.route_planner_daily_limit_error_message,
    icon = SharedRes.images.ic_error,
)

fun NetworkError.toRoutePlanInfoViewData(): InfoViewData =
    when (this) {
        NetworkError.RATE_LIMITED -> routePlanDailyLimitInfoViewData
        else -> toInfoViewData()
    }

fun RoutePlan.toGpxWaypoints(): List<GpxWaypoint> = waypoints.toGpxWaypoints()

fun List<Location>.toGpxWaypoints(): List<GpxWaypoint> {
    val isRoundTrip = size >= 2 && first() == last()

    return mapIndexedNotNull { index, location ->
        when {
            index == 0 && isRoundTrip -> GpxWaypoint(location, WaypointType.ROUND_TRIP)
            index == 0 -> GpxWaypoint(location, WaypointType.START)
            index == lastIndex && isRoundTrip -> null
            index == lastIndex -> GpxWaypoint(location, WaypointType.END)
            else -> GpxWaypoint(location, WaypointType.INTERMEDIATE)
        }
    }
}

private fun List<List<Double>>.toLocations(): List<Location> =
    mapNotNull { coordinates ->
        val longitude = coordinates.getOrNull(LONGITUDE_INDEX) ?: return@mapNotNull null
        val latitude = coordinates.getOrNull(LATITUDE_INDEX) ?: return@mapNotNull null

        Location(
            latitude = latitude,
            longitude = longitude,
            altitude = coordinates.getOrNull(ALTITUDE_INDEX),
        )
    }
