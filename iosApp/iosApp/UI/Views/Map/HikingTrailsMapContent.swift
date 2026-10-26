@preconcurrency import MapboxMaps
import Shared
import SwiftUI

struct HikingTrailsMapContent: MapContent {
    let baseLayer: Shared.BaseLayer

    private let layer = OverlayLayer.turistautak
    private let outdoorsSlotId = "\(OverlayLayer.turistautak.layerId)-slot"

    var body: some MapContent {
        RasterSource(id: layer.layerId)
            .tiles(layer.tiles)
            .tileSize(Double(layer.tileSize))
            .minzoom(Double(layer.minZoom))
            .maxzoom(Double(layer.maxZoom))
        if baseLayer == .outdoors {
            // Classic styles have no slots, and only a slot layer can be placed at a custom position.
            SlotLayer(id: outdoorsSlotId)
                .position(.below(MapConstants.shared.OUTDOORS_HIKING_LAYER_BELOW_ID))
            rasterLayer
                .slot(Slot(stringLiteral: outdoorsSlotId))
        } else {
            rasterLayer
                .slot(.middle)
        }
    }

    private var rasterLayer: RasterLayer {
        RasterLayer(id: layer.layerId, source: layer.layerId)
            .rasterEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }
}
