@preconcurrency import MapboxMaps
import Shared
import SwiftUI

struct TuHuMapContent: MapContent {
    let scaleFactor: Float

    var body: some MapContent {
        let tileSize = TuHuBaseMap.shared.tileSize(scaleFactor: scaleFactor)
        let sourceId = TuHuBaseMap.shared.layerId(tileSize: tileSize)
        source(id: sourceId, tileSize: tileSize)
        RasterLayer(id: sourceId, source: sourceId)
            .rasterEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }

    private func source(id: String, tileSize: Int64) -> RasterSource {
        var source = RasterSource(id: id)
            .tiles(TuHuBaseMap.shared.tiles)
            .tileSize(Double(tileSize))
            .minzoom(Double(TuHuBaseMap.shared.MIN_ZOOM))
            .maxzoom(Double(TuHuBaseMap.shared.MAX_ZOOM))
        // RasterSource has no attribution modifier
        source.attribution = TuHuBaseMap.shared.ATTRIBUTION
        return source
    }
}
