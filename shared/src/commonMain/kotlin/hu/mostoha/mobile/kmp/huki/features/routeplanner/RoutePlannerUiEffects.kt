package hu.mostoha.mobile.kmp.huki.features.routeplanner

import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan

sealed interface RoutePlannerUiEffects {
    data class RoutePlanUpdated(
        val routePlan: RoutePlan?,
        val markers: List<GpxWaypoint>,
    ) : RoutePlannerUiEffects
    data class RoutePlanSaved(val fileUri: String) : RoutePlannerUiEffects
    data object RoutePlanSaveFailed : RoutePlannerUiEffects
    data object MinimizeSheet : RoutePlannerUiEffects
    data object ExpandSheet : RoutePlannerUiEffects
    data object Close : RoutePlannerUiEffects
}
