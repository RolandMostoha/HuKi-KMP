package hu.mostoha.mobile.kmp.huki.ui.features.routeplanner

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiState
import hu.mostoha.mobile.kmp.huki.model.domain.InfoViewData
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.time.Duration.Companion.hours

object PreviewRoutePlanner {
    val myLocationWaypoint = RoutePlannerWaypoint(
        id = "my-location",
        name = StringDesc.Resource(SharedRes.strings.route_planner_my_location),
        location = Location(47.4979, 19.0402),
    )
    val namedWaypoint = RoutePlannerWaypoint(
        id = "ram-hegy",
        name = StringDesc.Raw("Rám-hegy"),
        location = Location(47.7193911, 18.8961602),
    )
    val secondNamedWaypoint = RoutePlannerWaypoint(
        id = "dobogoko",
        name = StringDesc.Raw("Dobogókő"),
        location = Location(47.7211, 18.9042),
    )
    val emptyWaypoint = RoutePlannerWaypoint(id = "empty")

    val routePlan = RoutePlan(
        waypoints = listOf(myLocationWaypoint.location!!, namedWaypoint.location!!),
        locations = listOf(myLocationWaypoint.location!!, namedWaypoint.location!!),
        routeStats = RouteStats(
            travelTime = 7.hours,
            distance = 24.6.kilometers,
            incline = 820,
            decline = 760,
        ),
    )

    fun manyStops(count: Int): List<RoutePlannerWaypoint> =
        listOf(myLocationWaypoint) +
            (1..count).map { RoutePlannerWaypoint(id = "stop-$it", name = StringDesc.Raw("Stop $it")) } +
            secondNamedWaypoint

    fun uiState(
        stops: List<RoutePlannerWaypoint> = listOf(myLocationWaypoint, namedWaypoint),
        routePlan: RoutePlan? = null,
        isLoading: Boolean = false,
        error: InfoViewData? = null,
    ): RoutePlannerUiState =
        RoutePlannerUiState(
            routePlan = routePlan,
            isRoutePlanLoading = isLoading,
            routePlanError = error,
        ).withStops(stops)
}
