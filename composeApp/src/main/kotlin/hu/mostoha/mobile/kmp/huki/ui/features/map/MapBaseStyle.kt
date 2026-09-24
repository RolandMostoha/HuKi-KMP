package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.MapboxStyleComposable
import com.mapbox.maps.extension.compose.style.StyleColorTheme
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

@Composable
@MapboxStyleComposable
@OptIn(MapboxExperimental::class)
fun MapBaseStyle(baseLayer: BaseLayer, isDarkMode: Boolean) {
    val lightPreset = if (isDarkMode) LightPresetValue.NIGHT else LightPresetValue.DAY

    when (baseLayer) {
        BaseLayer.OUTDOORS -> {
            val colorTheme = rememberColorTheme(base64 = OutdoorsColorTheme.DARK_MAP_LUT_BASE64)
            val darkColorTheme = rememberStyleColorTheme(colorTheme)

            MapStyle(
                style = Style.OUTDOORS,
                styleState = rememberStyleState {
                    styleColorTheme = if (isDarkMode) darkColorTheme else StyleColorTheme.STYLE_DEFAULT
                },
            )
        }
        BaseLayer.CITY -> {
            MapboxStandardStyle(
                standardStyleState = rememberStandardStyleState {
                    configurationsState.lightPreset = lightPreset
                },
            )
        }
        BaseLayer.SATELLITE -> {
            MapboxStandardSatelliteStyle(
                standardSatelliteStyleState = rememberStandardSatelliteStyleState {
                    configurationsState.lightPreset = lightPreset
                },
            )
        }
    }
}
