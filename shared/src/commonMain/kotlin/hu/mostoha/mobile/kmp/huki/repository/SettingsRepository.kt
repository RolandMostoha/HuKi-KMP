package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.MapLabelScale
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserPreferences>

    suspend fun setMapZoomControlsVisible(visible: Boolean)

    suspend fun setMapRotationEnabled(enabled: Boolean)

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setBaseLayer(baseLayer: BaseLayer)

    suspend fun setMapLabelScale(mapLabelScale: MapLabelScale)
    suspend fun setGpxDistancesVisible(visible: Boolean)

    suspend fun setGpxSlopeColorsVisible(visible: Boolean)
}
