@preconcurrency import MapboxMaps
import Shared
import SwiftUI

struct RasterBaseMapContent: MapContent {
    let rasterBaseMap: RasterBaseMap
    let scaleFactor: Float

    var body: some MapContent {
        let tileSize = rasterBaseMap.tileSize(scaleFactor: scaleFactor)
        let sourceId = rasterBaseMap.layerId(tileSize: tileSize)
        source(id: sourceId, tileSize: tileSize)
        RasterLayer(id: sourceId, source: sourceId)
            .rasterEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }

    private func source(id: String, tileSize: Int64) -> RasterSource {
        var source = RasterSource(id: id)
            .tiles(rasterBaseMap.tiles)
            .tileSize(Double(tileSize))
            .minzoom(Double(rasterBaseMap.minZoom))
            .maxzoom(Double(rasterBaseMap.maxZoom))
        // RasterSource has no attribution modifier
        source.attribution = rasterBaseMap.attribution
        return source
    }
}
