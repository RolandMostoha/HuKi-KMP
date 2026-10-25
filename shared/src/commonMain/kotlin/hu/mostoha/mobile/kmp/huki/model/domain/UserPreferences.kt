package hu.mostoha.mobile.kmp.huki.model.domain

data class UserPreferences(
    val mapZoomControlsVisible: Boolean,
    val mapRotationEnabled: Boolean,
    val themeMode: ThemeMode,
    val baseLayer: BaseLayer,
    val mapLabelScale: MapLabelScale,
    val gpxDistancesVisible: Boolean,
) {
    companion object {
        val DEFAULTS = UserPreferences(
            mapZoomControlsVisible = false,
            mapRotationEnabled = true,
            themeMode = ThemeMode.SYSTEM,
            baseLayer = BaseLayer.OUTDOORS,
            mapLabelScale = MapLabelScale.DEFAULT,
            gpxDistancesVisible = true,
        )
    }
}
