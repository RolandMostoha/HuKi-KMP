@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct GpxRouteMapContent: MapContent {
    let gpxDetails: GpxDetails

    var body: some MapContent {
        let feature = Feature(geometry: .lineString(gpxDetails.locations.lineString))

        GeoJSONSource(id: gpxDetails.layerId)
            .data(.feature(feature))

        LineLayer(id: gpxDetails.layerId, source: gpxDetails.layerId)
            .lineWidth(SharedDimens.shared.GPX_LINE_WIDTH)
            .lineColor(SharedRes.colors().primary.getUIColor())
            .lineBorderWidth(SharedDimens.shared.GPX_STROKE_WIDTH)
            .lineBorderColor(SharedRes.colors().mapStroke.getUIColor())
            .lineColorUseTheme(.none)
            .lineBorderColorUseTheme(.none)
            .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }
}
