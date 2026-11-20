@preconcurrency import MapboxMaps
import Shared
import SwiftUI

struct OverlaysMapContent: MapContent {
    let baseLayer: Shared.BaseLayer
    let overlays: [OverlayLayer]

    private let outdoorsSlotId = "overlays-slot"

    var body: some MapContent {
        ForEvery(overlays, id: \.layerId) { overlay in
            source(overlay)
        }
        if baseLayer == .outdoors && !overlays.isEmpty {
            // Classic styles have no slots, and only a slot layer can be placed at a custom position.
            SlotLayer(id: outdoorsSlotId)
                .position(.below(MapConstants.shared.OUTDOORS_HIKING_LAYER_BELOW_ID))
        }
        // Own ids per slot and raster base map, so switching re-adds the layers above the new base layer.
        ForEvery(overlays, id: \.layerId) { overlay in
            switch baseLayer {
            case .outdoors:
                rasterLayer(overlay, id: "\(overlay.layerId)-outdoors")
                    .slot(Slot(stringLiteral: outdoorsSlotId))
            case .tuhu, .mapnik:
                rasterLayer(overlay, id: "\(overlay.layerId)-\(baseLayer.rasterBaseMap?.id ?? "raster")")
            default:
                rasterLayer(overlay, id: overlay.layerId)
                    .slot(.middle)
            }
        }
    }

    private func source(_ overlay: OverlayLayer) -> RasterSource {
        var source = RasterSource(id: overlay.layerId)
            .tiles(overlay.tiles)
            .tileSize(Double(overlay.tileSize))
            .minzoom(Double(overlay.minZoom))
            .maxzoom(Double(overlay.maxZoom))
            .prefetchZoomDelta(Double(overlay.prefetchZoomDelta))
            .maxOverscaleFactorForParentTiles(Double(overlay.maxOverscaleFactorForParentTiles))
        // RasterSource has no attribution modifier
        source.attribution = overlay.attribution
        return source
    }

    private func rasterLayer(_ overlay: OverlayLayer, id: String) -> RasterLayer {
        RasterLayer(id: id, source: overlay.layerId)
            .rasterEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }
}
