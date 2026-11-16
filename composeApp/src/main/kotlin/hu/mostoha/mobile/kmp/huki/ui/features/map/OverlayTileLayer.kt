package hu.mostoha.mobile.kmp.huki.ui.features.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.LongValue
import com.mapbox.maps.extension.compose.style.StringListValue
import com.mapbox.maps.extension.compose.style.StringValue
import com.mapbox.maps.extension.compose.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.compose.style.sources.generated.rememberRasterSourceState
import hu.mostoha.mobile.kmp.huki.model.domain.OverlayLayer
import hu.mostoha.mobile.kmp.huki.theme.MapLighting

@Composable
@MapboxMapComposable
fun OverlayTileLayer(overlay: OverlayLayer) {
    RasterLayer(
        layerId = overlay.layerId,
        sourceState = rememberRasterSourceState {
            tileSize = LongValue(overlay.tileSize)
            tiles = StringListValue(overlay.tiles)
            minZoom = LongValue(overlay.minZoom)
            maxZoom = LongValue(overlay.maxZoom)
            prefetchZoomDelta = LongValue(overlay.prefetchZoomDelta)
            maxOverscaleFactorForParentTiles = LongValue(overlay.maxOverscaleFactorForParentTiles)
            overlay.attribution?.let { attribution = StringValue(it) }
        },
    ) {
        rasterEmissiveStrength = DoubleValue(MapLighting.OVERLAY_EMISSIVE_STRENGTH)
    }
}

@Composable
@MapboxMapComposable
fun OverlayTileLayers(overlays: List<OverlayLayer>) {
    // A layer added later is placed at the anchor, not after its siblings, so all are re-added to keep the order
    key(overlays) {
        overlays.forEach { overlay -> OverlayTileLayer(overlay) }
    }
}
