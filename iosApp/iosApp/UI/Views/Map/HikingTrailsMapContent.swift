@preconcurrency import MapboxMaps
import Shared
import SwiftUI

struct HikingTrailsMapContent: MapContent {
    private let layer = OverlayLayer.turistautak

    var body: some MapContent {
        RasterSource(id: layer.layerId)
            .tiles(layer.tiles)
            .tileSize(Double(layer.tileSize))
            .minzoom(Double(layer.minZoom))
            .maxzoom(Double(layer.maxZoom))
        RasterLayer(id: layer.layerId, source: layer.layerId)
            .rasterEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }
}
