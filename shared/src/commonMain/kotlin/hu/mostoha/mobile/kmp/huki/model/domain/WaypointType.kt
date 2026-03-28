package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class WaypointType(val icon: ImageResource) {
    START(icon = SharedRes.images.ic_gpx_start),
    END(icon = SharedRes.images.ic_gpx_end),
    INTERMEDIATE(icon = SharedRes.images.ic_gpx_waypoint),
    ROUND_TRIP(icon = SharedRes.images.ic_gpx_round_trip),
}
