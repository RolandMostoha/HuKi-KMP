package hu.mostoha.mobile.kmp.huki.features.routeplanner

sealed interface RoutePlannerUiEffects {
    data class RoutePlanSaved(val fileUri: String) : RoutePlannerUiEffects
    data object RoutePlanSaveFailed : RoutePlannerUiEffects
    data object MinimizeSheet : RoutePlannerUiEffects
    data object ExpandSheet : RoutePlannerUiEffects
    data object Close : RoutePlannerUiEffects
}
