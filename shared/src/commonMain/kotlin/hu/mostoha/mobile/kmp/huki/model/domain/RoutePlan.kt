package hu.mostoha.mobile.kmp.huki.model.domain

data class RoutePlan(
    val waypoints: List<Location>,
    val locations: List<Location>,
    val routeStats: RouteStats,
)
