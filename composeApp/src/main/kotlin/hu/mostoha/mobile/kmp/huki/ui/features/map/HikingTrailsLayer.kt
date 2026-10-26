package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.LongValue
import com.mapbox.maps.extension.compose.style.StringListValue
import com.mapbox.maps.extension.compose.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.compose.style.sources.generated.rememberRasterSourceState
import hu.mostoha.mobile.kmp.huki.model.domain.OverlayLayer
import hu.mostoha.mobile.kmp.huki.theme.MapLighting

@Composable
@MapboxMapComposable
fun HikingTrailsLayer() {
    RasterLayer(
        layerId = OverlayLayer.TURISTAUTAK.layerId,
        sourceState = rememberRasterSourceState {
            tileSize = LongValue(OverlayLayer.TURISTAUTAK.tileSize)
            tiles = StringListValue(OverlayLayer.TURISTAUTAK.tiles)
            minZoom = LongValue(OverlayLayer.TURISTAUTAK.minZoom)
            maxZoom = LongValue(OverlayLayer.TURISTAUTAK.maxZoom)
        },
    ) {
        rasterEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
}
