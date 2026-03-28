package hu.mostoha.mobile.kmp.huki.model.domain

data class GpxWaypoint(
    val location: Location,
    val type: WaypointType,
    val name: String? = null,
    val description: String? = null,
)
