package hu.mostoha.mobile.kmp.huki.model.domain

enum class RoutingMode {
    /**
     * GraphHopper first, HuKi-Routing for routes inside Hungary near or after the daily GraphHopper limit.
     */
    AUTO,
    GRAPHHOPPER_ONLY,
    HUKI_ROUTING_ONLY,
}
