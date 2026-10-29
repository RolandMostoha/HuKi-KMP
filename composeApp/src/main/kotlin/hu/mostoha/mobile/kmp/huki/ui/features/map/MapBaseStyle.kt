package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.maps.extension.compose.style.MapboxStyleComposable
import com.mapbox.maps.extension.compose.style.StyleColorTheme
import com.mapbox.maps.extension.compose.style.layerPositionedContent
import com.mapbox.maps.extension.compose.style.rememberColorTheme
import com.mapbox.maps.extension.compose.style.rememberStyleColorTheme
import com.mapbox.maps.extension.compose.style.rememberStyleState
import com.mapbox.maps.extension.compose.style.standard.LightPresetValue
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardSatelliteStyle
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.compose.style.standard.rememberStandardSatelliteStyleState
import com.mapbox.maps.extension.compose.style.standard.rememberStandardStyleState
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.theme.OutdoorsColorTheme
import hu.mostoha.mobile.kmp.huki.util.MapConstants

private typealias MapSlotContent = @Composable () -> Unit

@Composable
@MapboxStyleComposable
fun MapBaseStyle(baseLayer: BaseLayer, isDarkMode: Boolean, isHikingLayerVisible: Boolean) {
    val lightPreset = if (isDarkMode) LightPresetValue.NIGHT else LightPresetValue.DAY
    val hikingSlotContent: MapSlotContent? = if (isHikingLayerVisible) {
        { HikingTrailsLayer() }
    } else {
        null
    }

    when (baseLayer) {
        BaseLayer.OUTDOORS -> {
            val colorTheme = rememberColorTheme(base64 = OutdoorsColorTheme.DARK_MAP_LUT_BASE64)
            val darkColorTheme = rememberStyleColorTheme(colorTheme)

            GenericStyle(
                style = Style.OUTDOORS,
                layerPositionedContent = remember(isHikingLayerVisible) {
                    layerPositionedContent {
                        if (isHikingLayerVisible) {
                            belowLayer(MapConstants.OUTDOORS_HIKING_LAYER_BELOW_ID) { HikingTrailsLayer() }
                        }
                    }
                },
                styleState = rememberStyleState {
                    styleColorTheme = if (isDarkMode) darkColorTheme else StyleColorTheme.STYLE_DEFAULT
                },
            )
        }
        BaseLayer.CITY -> {
            MapboxStandardStyle(
                middleSlot = hikingSlotContent,
                standardStyleState = rememberStandardStyleState {
                    configurationsState.lightPreset = lightPreset
                },
            )
        }
        BaseLayer.SATELLITE -> {
            MapboxStandardSatelliteStyle(
                middleSlot = hikingSlotContent,
                standardSatelliteStyleState = rememberStandardSatelliteStyleState {
                    configurationsState.lightPreset = lightPreset
                },
            )
        }
    }
}
