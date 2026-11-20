package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
import hu.mostoha.mobile.kmp.huki.model.domain.OverlayLayer
import hu.mostoha.mobile.kmp.huki.model.domain.RasterBaseMap
import hu.mostoha.mobile.kmp.huki.theme.OutdoorsColorTheme
import hu.mostoha.mobile.kmp.huki.util.MapConstants

private typealias MapSlotContent = @Composable () -> Unit

@Composable
@MapboxStyleComposable
fun MapBaseStyle(
    baseLayer: BaseLayer,
    isDarkMode: Boolean,
    overlays: List<OverlayLayer>,
    tileScaleFactor: Float,
) {
    val lightPreset = if (isDarkMode) LightPresetValue.NIGHT else LightPresetValue.DAY
    val overlaySlotContent: MapSlotContent? = if (overlays.isNotEmpty()) {
        { OverlayTileLayers(overlays) }
    } else {
        null
    }

    when (baseLayer) {
        BaseLayer.OUTDOORS -> {
            val colorTheme = rememberColorTheme(base64 = OutdoorsColorTheme.DARK_MAP_LUT_BASE64)
            val darkColorTheme = rememberStyleColorTheme(colorTheme)

            GenericStyle(
                style = Style.OUTDOORS,
                layerPositionedContent = remember(overlays) {
                    layerPositionedContent {
                        if (overlays.isNotEmpty()) {
                            belowLayer(MapConstants.OUTDOORS_HIKING_LAYER_BELOW_ID) { OverlayTileLayers(overlays) }
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
                middleSlot = overlaySlotContent,
                standardStyleState = rememberStandardStyleState {
                    configurationsState.lightPreset = lightPreset
                },
            )
        }
        BaseLayer.SATELLITE -> {
            MapboxStandardSatelliteStyle(
                middleSlot = overlaySlotContent,
                standardSatelliteStyleState = rememberStandardSatelliteStyleState {
                    configurationsState.lightPreset = lightPreset
                },
            )
        }
        BaseLayer.TUHU, BaseLayer.MAPNIK -> {
            val rasterBaseMap = requireNotNull(baseLayer.rasterBaseMap)
            val tileSize = rasterBaseMap.tileSize(tileScaleFactor)
            GenericStyle(
                style = RasterBaseMap.STYLE_JSON,
                layerPositionedContent = remember(rasterBaseMap, overlays, tileSize) {
                    layerPositionedContent {
                        aboveLayer(RasterBaseMap.BACKGROUND_LAYER_ID) {
                            // Re-added with the overlays, or a new overlay lands under the raster tiles
                            key(rasterBaseMap, overlays) {
                                RasterBaseMapLayer(rasterBaseMap, tileSize)
                                OverlayTileLayers(overlays)
                            }
                        }
                    }
                },
            )
        }
    }
}
