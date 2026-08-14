package hu.mostoha.mobile.kmp.huki.model.domain

data class RoutePlanRequest(
    val routeProfile: RoutePlannerProfile,
    val waypoints: List<Location>,
)
