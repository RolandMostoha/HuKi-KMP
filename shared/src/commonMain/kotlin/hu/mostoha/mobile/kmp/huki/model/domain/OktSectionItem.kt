package hu.mostoha.mobile.kmp.huki.model.domain

data class OktSectionItem(
    val id: String,
    val badgePrefix: String,
    val badgeNumber: String?,
    val name: String,
    val routeStats: RouteStats,
    val stamps: List<OktStampItem>,
    val reversedStamps: List<OktStampItem>,
    val websiteUrl: String,
)

data class OktStampItem(
    val marker: OktMarker,
    val tag: String,
    val legDistance: String,
)
