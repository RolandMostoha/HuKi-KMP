package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class RoutePlannerProfile(val title: StringResource) {
    ON_TRAILS(title = SharedRes.strings.route_planner_profile_on_trails),
    SHORTEST_ROUTE(title = SharedRes.strings.route_planner_profile_shortest_route),
    BIKE(title = SharedRes.strings.route_planner_profile_bike),
}
