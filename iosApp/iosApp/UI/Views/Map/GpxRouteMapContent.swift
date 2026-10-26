@preconcurrency @_spi(Experimental) import MapboxMaps
import Shared
import SwiftUI

struct GpxRouteMapContent: MapContent {
    let gpxDetails: GpxDetails
    var colorScheme: ColorScheme = .light
    var slopeColorsVisible: Bool = false

    private var slopeSourceId: String { "\(gpxDetails.layerId)_slope" }

    var body: some MapContent {
        GeoJSONSource(id: gpxDetails.layerId)
            .data(.feature(Feature(geometry: .lineString(gpxDetails.locations.lineString))))

        if slopeColorsVisible, gpxDetails.hasSlopeSegments {
            GeoJSONSource(id: slopeSourceId)
                .data(.featureCollection(SlopeFeatureCache.shared.features(for: gpxDetails, colorScheme: colorScheme)))
            lineLayer(id: "\(gpxDetails.layerId)_slope_casing", source: gpxDetails.layerId)
                .lineColor(SharedRes.colors().markerStroke.getUIColor())
            lineLayer(id: slopeSourceId, source: slopeSourceId)
                .lineWidth(SharedDimens.shared.GPX_LINE_WIDTH - 2 * SharedDimens.shared.MAP_LINE_STROKE_WIDTH)
                .lineColor(Exp(.get) { SlopeSegmentFeature.colorProperty })
        } else {
            lineLayer(id: gpxDetails.layerId, source: gpxDetails.layerId)
                .lineColor(SharedRes.colors().primary.getUIColor())
                .lineBorderWidth(SharedDimens.shared.MAP_LINE_STROKE_WIDTH)
                .lineBorderColor(SharedRes.colors().mapStroke.getUIColor())
                .lineBorderColorUseTheme(.none)
        }

        RouteDirectionMapContent(sourceId: gpxDetails.layerId, layerId: "\(gpxDetails.layerId)_direction")
    }

    private func lineLayer(id: String, source: String) -> LineLayer {
        LineLayer(id: id, source: source)
            .lineWidth(SharedDimens.shared.GPX_LINE_WIDTH)
            .lineCap(.round)
            .lineJoin(.round)
            .lineColorUseTheme(.none)
            .lineEmissiveStrength(MapLighting.shared.OVERLAY_EMISSIVE_STRENGTH)
    }

}

private enum SlopeSegmentFeature {
    static let colorProperty = "color"
}

// The map body is re-evaluated on every UI state change, e.g. each location update
private final class SlopeFeatureCache: @unchecked Sendable {
    static let shared = SlopeFeatureCache()

    private let lock = NSLock()
    private var key: String?
    private var cached = FeatureCollection(features: [])

    func features(for gpxDetails: GpxDetails, colorScheme: ColorScheme) -> FeatureCollection {
        lock.lock()
        defer { lock.unlock() }
        let key = "\(gpxDetails.layerId)_\(colorScheme)"
        if key == self.key { return cached }

        let traits = UITraitCollection(userInterfaceStyle: colorScheme == .dark ? .dark : .light)
        let flatColor = SharedRes.colors().primary.getUIColor().resolvedColor(with: traits).rgb
        let colorScale = SlopeColorScale(flatColor: flatColor)
        cached = FeatureCollection(features: gpxDetails.slopeSegments.map { segment in
            var feature = Feature(geometry: .lineString(segment.locations.lineString))
            feature.properties = [
                SlopeSegmentFeature.colorProperty: .string(colorScale.colorHex(slopePercent: segment.slopePercent))
            ]
            return feature
        })
        self.key = key
        return cached
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

private extension UIColor {
    var rgb: Int32 {
        var red: CGFloat = 0, green: CGFloat = 0, blue: CGFloat = 0, alpha: CGFloat = 0
        getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        return [red, green, blue].reduce(Int32(0)) { rgb, channel in
            rgb << 8 | Int32(min(max((channel * 255).rounded(), 0), 255))
        }
    }
}
