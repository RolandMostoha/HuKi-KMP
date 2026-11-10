package hu.mostoha.mobile.kmp.huki.model.domain

data class RoutePlan(
    val waypoints: List<Location>,
    val locations: List<Location>,
    val routeStats: RouteStats,
) {
    /**
     * Geometry is counted, as the UiState is logged on every change and a long route makes the full string slow.
     */
    override fun toString(): String =
        "RoutePlan(waypoints=${waypoints.size}, locations=${locations.size}, routeStats=$routeStats)"
}
