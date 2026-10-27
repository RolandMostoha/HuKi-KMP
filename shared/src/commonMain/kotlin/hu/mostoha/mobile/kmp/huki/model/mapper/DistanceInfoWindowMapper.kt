package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.RouteProgress
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter

fun GpxWaypoint.toDistanceInfoWindow(id: Int, progress: RouteProgress?): DistanceInfoWindowData =
    DistanceInfoWindowData(
        id = id,
        location = location,
        name = name?.trim()?.takeIf { it.isNotEmpty() },
        label = when (type) {
            WaypointType.START -> StringDesc.Resource(SharedRes.strings.gpx_distance_info_window_start)
            WaypointType.END -> StringDesc.Resource(SharedRes.strings.gpx_distance_info_window_end)
            WaypointType.INTERMEDIATE, WaypointType.ROUND_TRIP -> null
        },
        distance = progress?.let { DistanceFormatter.formatDistance(it.distance) },
        travelTime = progress?.let { TravelTimeFormatter.formatTravelTime(it.travelTime) },
        priority = when (type) {
            WaypointType.END, WaypointType.ROUND_TRIP -> 3
            WaypointType.START -> 2
            WaypointType.INTERMEDIATE -> 1
        },
    )
