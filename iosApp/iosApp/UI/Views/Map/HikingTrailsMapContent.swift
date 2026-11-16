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
            .prefetchZoomDelta(Double(layer.prefetchZoomDelta))
            .maxOverscaleFactorForParentTiles(Double(layer.maxOverscaleFactorForParentTiles))
        // Own ids per slot, so switching styles re-adds the layer instead of moving it between slots.
        switch baseLayer {
        case .outdoors:
            // Classic styles have no slots, and only a slot layer can be placed at a custom position.
            SlotLayer(id: outdoorsSlotId)
                .position(.below(MapConstants.shared.OUTDOORS_HIKING_LAYER_BELOW_ID))
            rasterLayer(id: "\(layer.layerId)-outdoors")
                .slot(Slot(stringLiteral: outdoorsSlotId))
        case .tuhu:
            rasterLayer(id: "\(layer.layerId)-tuhu")
        default:
            rasterLayer(id: layer.layerId)
                .slot(.middle)
        }
    }

    private func rasterLayer(id: String) -> RasterLayer {
        RasterLayer(id: id, source: layer.layerId)
            .rasterEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }
}
