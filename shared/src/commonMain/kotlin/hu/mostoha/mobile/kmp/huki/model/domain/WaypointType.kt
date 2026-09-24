package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class WaypointType(
    val icon: ImageResource,
    val outdoorsDarkIcon: ImageResource,
) {
    START(
        icon = SharedRes.images.ic_gpx_start,
        outdoorsDarkIcon = SharedRes.images.ic_gpx_start_outdoors_dark,
    ),
    END(
        icon = SharedRes.images.ic_gpx_end,
        outdoorsDarkIcon = SharedRes.images.ic_gpx_end_outdoors_dark,
    ),
    INTERMEDIATE(
        icon = SharedRes.images.ic_gpx_waypoint,
        outdoorsDarkIcon = SharedRes.images.ic_gpx_waypoint_outdoors_dark,
    ),
    ROUND_TRIP(
        icon = SharedRes.images.ic_gpx_round_trip,
        outdoorsDarkIcon = SharedRes.images.ic_gpx_round_trip_outdoors_dark,
    ),
}
