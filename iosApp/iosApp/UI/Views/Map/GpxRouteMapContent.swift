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
            .lineBorderWidth(SharedDimens.shared.MAP_LINE_STROKE_WIDTH)
            .lineBorderColor(SharedRes.colors().mapStroke.getUIColor())
            .lineCap(.round)
            .lineJoin(.round)
            .lineColorUseTheme(.none)
            .lineBorderColorUseTheme(.none)
            .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)

        RouteDirectionMapContent(sourceId: gpxDetails.layerId, layerId: "\(gpxDetails.layerId)_direction")
    }
}

struct RouteDirectionMapContent: MapContent {
    let sourceId: String
    let layerId: String

    var body: some MapContent {
        StyleImage(id: RouteDirectionArrow.imageId, image: RouteDirectionArrow.image, sdf: true)

        SymbolLayer(id: layerId, source: sourceId)
            .minZoom(Double(SharedDimens.shared.GPX_DIRECTION_ARROW_MIN_ZOOM))
            .symbolPlacement(.line)
            .symbolSpacing(SharedDimens.shared.GPX_DIRECTION_ARROW_SPACING)
            .iconImage(RouteDirectionArrow.imageId)
            // An SDF icon takes its color from iconColor, which can opt out of the dark map's color grading
            .iconColor(SharedRes.colors().mapStroke.getUIColor())
            .iconColorUseTheme(.none)
            .iconEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
            .iconRotationAlignment(.map)
            // Upright icons flip on westbound segments, which would point arrows the wrong way
            .iconKeepUpright(false)
            .iconAllowOverlap(true)
            .iconIgnorePlacement(true)
    }
}

private enum RouteDirectionArrow {
    static let imageId = "route_direction_arrow"

    // A notched arrowhead pointing along +x, the direction of the line in a line-placed symbol
    static let image: UIImage = {
        let length = SharedDimens.shared.GPX_DIRECTION_ARROW_LENGTH
        let width = SharedDimens.shared.GPX_DIRECTION_ARROW_WIDTH
        let notch = length * SharedDimens.shared.GPX_DIRECTION_ARROW_NOTCH_RATIO
        return UIGraphicsImageRenderer(size: CGSize(width: length, height: width)).image { _ in
            let arrowhead = UIBezierPath()
            arrowhead.move(to: .zero)
            arrowhead.addLine(to: CGPoint(x: length, y: width / 2))
            arrowhead.addLine(to: CGPoint(x: 0, y: width))
            arrowhead.addLine(to: CGPoint(x: notch, y: width / 2))
            arrowhead.close()
            UIColor.white.setFill()
            arrowhead.fill()
        }
    }()
}
