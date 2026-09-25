package hu.mostoha.mobile.kmp.huki.model.domain

data class UserPreferences(
    val mapZoomControlsVisible: Boolean,
    val themeMode: ThemeMode,
    val baseLayer: BaseLayer,
) {
    companion object {
        val DEFAULTS = UserPreferences(
            mapZoomControlsVisible = false,
            themeMode = ThemeMode.SYSTEM,
            baseLayer = BaseLayer.OUTDOORS,
        )
    }
}
